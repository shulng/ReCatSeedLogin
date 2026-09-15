# 命名标准化实施计划

## Context（背景与目标）

项目已完成目录/类重构（前 4 个阶段），但存在明显的跨平台、跨层级命名不一致，影响可维护性与可读性。本次目标：**建立统一的分类体系（分类架构已基本成型）与命名规范，并按类别对全部项目元素实施命名标准化**。

用户确认范围覆盖：类/接口、常量、配置字段、方法/变量四类；配置字段**改为显式规范命名**（不回避改动面）；产出物为**规范文档 + 分批提交**，每批独立 commit，最终离线编译验证。

## 分类体系（已有结构，作为命名坐标系）

分层包结构（`cc.baka9.catseedlogin.`）：

* `common/` 平台无关共享层：`config` `api` `util` `model` `i18n` `communication` `platform` `proxy` `Version`

* `bukkit/` `bungee/` `velocity/` 平台适配层，各自含 `config/` `command/` `listener/` `net|communication/` 等子包（bukkit 另有 `cache/ object/ task/ scheduler/ database/ util/ event/ platform/ api/`）

## 命名规范（写入新增 NAMING.md）

| 元素       | 规范                                                                     |
| -------- | ---------------------------------------------------------------------- |
| 类/接口     | UpperCamelCase；动词能力接口可 `Xxxable`；接口名不做 `I` 前缀                          |
| 抽象类      | 前缀 `Abstract`（模板/钩子，如 `AbstractCommandSupport`）                        |
| 基类（共享实现） | 前缀 `Base`（如 `BaseConfigManager`、`BaseCommunication`）                   |
| 命令类      | `Command<领域>`，置于 `<平台>/command/`（跨平台统一）                                |
| 平台上下文单例  | `XxxContext`（如 `BukkitContext`），避免与 common 接口 `PluginContext` 同名       |
| 平台适配器    | `XxxPlatformAdapter`（已一致 `BukkitPlatformAdapter` 等）                    |
| 常量       | `SCREAMING_SNAKE`（全大写+下划线），`ConfigConstants.*` 已符合                     |
| 配置静态字段   | lowerCamelCase，与 YAML key 语义一致（如 `loginWithSameIp`、`emailSmtpHost`）    |
| 方法/变量    | lowerCamelCase；缩写规范：`IP→Ip`、`ID→Id`、`URL→Url`（非首词）；布尔 getter 用 `isXxx` |
| 工具类      | `XxxUtil`（无状态静态），专用工具保留领域名（如 `Crypt`）                                  |
| 长度/字符    | 名称 ≤ 64 字符；仅用 ASCII 字母/数字/下划线；禁止 `$`、空格                                |

## 实施批次（每批一个 commit，逐批离线编译 `mvn -o -DskipTests compile`）

### Batch A：类/接口命名统一

1. **命令类跨平台统一**（清除 `Commands`/`BungeeCommands`/`CommandCatSeedLogin` 形态差异）：

   * `velocity/Commands.java` → `velocity/command/CommandCatSeedLogin.java`（包+类名，更新 `PluginMain.java` 引用）

   * `bungee/BungeeCommands.java` → `bungee/command/CommandCatSeedLogin.java`（更新 `bungee/PluginMain.java` 引用）

   * bukkit `command/CommandXxx` 已符合，不动
2. **消除同名歧义**：`bukkit/platform/PluginContext.java`（静态定位器单例）→ `bukkit/platform/BukkitContext.java`，类名与所有 `PluginContext.getXxx()` 调用点改为 `BukkitContext.getXxx()`。波及 11 处引用：`Communication.java`、5 个 `Command*.java`、`CatSeedLogin.java`、`LoginPlayerHelper.java`（见上方 grep 清单）。

   * `common/platform/PluginContext.java`（接口）保持不变。

### Batch B：常量命名审查

* `ConfigConstants.*` 已 SCREAMING\_SNAKE；`MessageKey.*` 已符合。仅校对遗漏项：确认 `Path`/`Comment` 内无 ALL\_CAPS 无下划线常量。预期几乎零改动，仅记录文档。

### Batch C：配置字段显式规范命名（改动面最大；138 处/14 文件）

* `bukkit/config/Config.java` 嵌套静态字段 lowerCamelCase 化（字段名→新名示例）：

  * `MySQL`：`Enable→enable` `Host→host` `Port→port` `Database→database` `User→user` `Password→password`

  * `BungeeCord`：`Enable→enable` `Host→host` `Port→port` `AuthKey→authKey`

  * `Settings`：`IpRegisterCountLimit→ipRegisterCountLimit`、`LoginwiththesameIP→loginWithSameIp`、`IPTimeout→ipTimeout`、`LimitChineseID→limitChineseId`、`MinLengthID→minLengthId`、`MaxLengthID→maxLengthId`、`CanTpSpawnLocation→canTpSpawnLocation`、`CommandWhiteList→commandWhiteList`、`AutoKick→autoKick`、`NamePattern→namePattern`、`ReenterInterval→reenterInterval`、`SpawnLocation→spawnLocation`、`FloodgatePrefixProtect→floodgatePrefixProtect`、`DeathStateQuitRecordLocation→deathStateQuitRecordLocation`、`EmptyBackpack→emptyBackpack`、`BedrockLoginBypass→bedrockLoginBypass`、`BeforeLoginNoDamage→beforeLoginNoDamage`、`AfterLoginBack→afterLoginBack`

  * `EmailVerify`：`Enable→enable` `EmailAccount→emailAccount` `EmailPassword→emailPassword` `EmailSmtpHost→emailSmtpHost` `EmailSmtpPort→emailSmtpPort` `SSLAuthVerify→sslAuthVerify` `FromPersonal→fromPersonal`

  * `Language` 消息字段：全部改为 lowerCamelCase（`LOGIN_REQUEST→loginRequest` …），与 `MessageKey.LOGIN_REQUEST` 的常量引用保持一一对应

* 同步更新全部引用点（14 个 bukkit 文件，见下）：`CommandCatSeedLogin.java`(45) `PlayerListener.java`(24) `CommandResetPassword.java`(16) `LoginPlayerHelper.java`(8) `CommandChangePassword.java`(8) `CommandLogin.java`(8) `EmailSender.java`(7) `TaskAutoKick.java`(4) `CommandRegister.java`(7) `MySQL.java`(5) `TaskSendLoginMessage.java`(2) `CommandBindEmail.java`(1)

* 关联：相关配置访问器方法如其仅有 `IP`/`ID` 缩写问题（如 `isLoginWithSameIP`、`getIPTimeout`），同步归一为 `Ip`/`Id`，保证字段与访问器一致。

### Batch D：规范文档 + 最终验证

* 新增 `NAMING.md`（分类体系 + 上述命名规范表 + 长度/字符/大小写/前后缀约定）

* `REFACTOR.md` 增补「阶段5：命名标准化」清单

* 最终 `mvn -o -DskipTests package` 全量打包验证

## 关键文件

* 命令类：`velocity/Commands.java`、`bungee/BungeeCommands.java`、对应 `PluginMain.java`

* 同名冲突：`bukkit/platform/PluginContext.java`（→BukkitContext）、`common/platform/PluginContext.java`（保留）

* 配置字段：`bukkit/config/Config.java` + 上列 13 个 bukkit 引用文件

* 文档：新增 `NAMING.md`，更新 `REFACTOR.md`

## 验证

* 每批执行 `mvn -o -DskipTests compile`，必须 BUILD SUCCESS

* Batch D 执行 `mvn -o -DskipTests package`（最终产物）

* `git log` 确认 4 个阶段 commit 各自独立、信息对应类别

* 确认三平台入口（`plugin.yml`/`bungee.yml`/`velocity-plugin.json` 声明的类）FQCN 未因改名受影响 —— 命令类名变更仅影响内部类名、不改变注册的命令字面量（`CatSeedLoginBungee`/`cslb`/`cslv` 等仍保留）；`BukkitContext` 不涉及入口类。

## 风险

* Batch C 改动面最大（138 处），全机械替换；每批编译兜底 + 逐文件核对，避免漏改 `Config.Language` 字段映射。

