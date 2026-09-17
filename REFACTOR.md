# 重构文档 (REFACTOR)

本文档记录 CatSeedLogin 全面结构优化与重构的过程、决策与变更清单。

> 状态：进行中。各阶段以 git commit 记录（分支 `awa`）。

## 目标
1. 重组清晰、模块化的项目目录结构
2. 优化类层次结构与接口定义，提升可维护性与扩展性
3. 按功能/业务逻辑系统性分类整理代码
4. 提取重复代码为可复用组件，减少冗余

遵循高内聚低耦合，保证可测试性；重构不改变插件行为。

## 阶段记录

### 阶段 1：目录重组（Commit 1）✅ 完成
- 纯文件移动 + package/import 更新，不改任何运行逻辑。
- 变更清单：
  - `common/config/PluginContext` → `common/platform/PluginContext`
  - bukkit 根包分派：`Cache`→`PlayerCache`(cache)、`Config`(config)、`Listeners`→`PlayerListener`/`ProtocolLibListeners`→`ProtocolLibListener`(listener)、`PluginContext`(platform)、`CatScheduler`(scheduler)、`Communication`(communication)、`CatSeedLoginAPI`(api)
  - bungee：`Listeners`→`BungeeListeners`(listener)、`BungeeCommunication`(net)
  - velocity：`Listeners`→`VelocityListeners`(listener)、`VelocityCommunication`(net)
- 验证：`mvn -o compile` → BUILD SUCCESS（仅存既有 deprecation 警告）。
- 入口类 `bukkit.CatSeedLogin` / `bungee.PluginMain` / `velocity.PluginMain` FQCN 未变。

### 阶段 2：类层次优化（Commit 2）✅ 完成
1. **SQL 基类**：`getConnection()/isConnectionValid()/closeConnection()` 上提至基类 `SQL`，新增抽象 `createConnection()`；`SQLite`/`MySQL` 仅实现建连差异，消除两处 `isConnectionValid` 重复与连接重建样板。
2. **AbstractCommandSupport（bukkit 命令基类）**：新增基类统一处理「非玩家守卫 + Floodgate 跳过」，子类实现 `onPlayerCommand(player,args)`；`CommandLogin/Register/ChangePassword/ResetPassword/BindEmail` 5 个玩家命令接入，`CommandCatSeedLogin`（控制台可用）不继承。
3. **ProxyLoginTracker（common/proxy）**：封装登录态列表与 `sendConnectRequest / sendKeepLoggedInRequest` 流程，`BungeeListeners`/`VelocityListeners` 改委托该组件，移除重复的 `CopyOnWriteArrayList` 管理逻辑。
- 验证：`mvn -o compile` → BUILD SUCCESS。

### 阶段 3：重复代码提取（Commit 3）✅ 完成
1. **洪水门跳过**：新增 `LoginPlayerHelper.isBedrockLoginBypassed(player)`，`AbstractCommandSupport` 统一调用；替换 5 个命令类中的 `Config.Settings.BedrockLoginBypass && LoginPlayerHelper.isFloodgatePlayer(player)` 表达式。
2. **口令变更持久化**：新增 `LoginPlayerHelper.changePasswordAndPersist(lp, rawPwd)` 封装 `PasswordHelper.updatePassword → sql.edit → PlayerCache.refresh`；Login(升 Argon2)/ChangePassword/ResetPassword 三处改为调用该组件。
3. **邮件发送编排**：`EmailSender.sendEmailAsync(to, subject, content, onSuccess, onFailure)` 统一异步发送+主线程回调；`EmailCode.DEFAULT_CODE_DURATION` 统一 `1000*60*5` 常量；ResetPassword/BindEmail 的发送与成功/失败通知改用该辅助，删除冗余的 `runTaskAsync/runTask` 样板。
- 验证：`mvn -o compile` → BUILD SUCCESS。

### 阶段 4：文档与收尾（Commit 4）
- 生成 `REFACTOR.md` 阶段记录（本文件）。
- 更新 `README.md`「项目架构」目录树为重构后的目标结构。
- 最终 `mvn -o compile` → BUILD SUCCESS；三平台入口 FQCN 与 `plugin.yml`/`bungee.yml`/`velocity-plugin.json` 均未改动。

### 阶段 5：命名标准化（Commit 5A / 5C / 5D）
分类体系与命名规范见 `NAMING.md`（Commit 5D）。按类别实际实施的更名：
- **全层级范围**：类/接口、常量、配置字段、方法/变量四类。
- **Commit 5A（类/接口）**
  - 命令类跨平台统一 → `Command<领域>` 置于 `<平台>/command/`：`velocity.Commands`→`velocity.command.CommandCatSeedLogin`、`bungee.BungeeCommands`→`bungee.command.CommandCatSeedLogin`（bukkit 命令类已合规）。
  - 消除同名歧义：`bukkit/platform/PluginContext`（静态定位器单例）→ `BukkitContext`，与 `common.platform.PluginContext`（接口）区分。
- **Commit 5B（常量）**：审计结论——`ConfigConstants.*`、`MessageKey.*` 已全部 `SCREAMING_SNAKE`，无违规模，零改动（并入文档）。
- **Commit 5C（配置字段/方法）**
  - `bukkit/config/Config.java` 嵌套静态字段全部 lowerCamelCase 化（服务器/Database/Email 命名空间，如 `LoginwiththesameIP→loginWithSameIp`、`SSLAuthVerify→sslAuthVerify`、`Enable→enable`）；同步更新 14 个引用文件。
  - 方法修正：`CommandCatSeedLogin.LoginwiththesameIP` → `loginWithSameIp`。
  - 说明：`CoreConfig`/`BaseConfigManager` 的 `getIPTimeout`、`isLoginWithSameIP` 等访问器为「接口+实现」一致约定的缩写（Google 风格允许），作为记录在案的例外保留，不在本阶段归一。
- **Commit 5D（文档/收尾）**
  - 新增 `NAMING.md`（分类体系 + 命名规则表）。
  - 更新本文件（阶段 5 记录）与 `README.md` 目录树。
  - 最终 `mvn -o -DskipTests package` → BUILD SUCCESS。

---

## 重构前后结构对照（摘要）

| 原位置 | 现位置 | 说明 |
| --- | --- | --- |
| `bukkit.Cache` | `bukkit.cache.PlayerCache` | 更名以明确职责 |
| `bukkit.Config` | `bukkit.config.Config` | 归入 config 包 |
| `bukkit.Listeners` | `bukkit.listener.PlayerListener` | 更名 |
| `bukkit.ProtocolLibListeners` | `bukkit.listener.ProtocolLibListener` | 更名 |
| `bukkit.PluginContext` | `bukkit.platform.PluginContext` | 归入 platform 包 |
| `bukkit.CatScheduler` | `bukkit.scheduler.CatScheduler` | 归入 scheduler 包 |
| `bukkit.Communication` | `bukkit.communication.Communication` | 归入 communication 包 |
| `bukkit.CatSeedLoginAPI` | `bukkit.api.CatSeedLoginAPI` | 归入 api 包 |
| `common.config.PluginContext` | `common.platform.PluginContext` | 迁入 platform，消除与 bukkit 同名混淆 |
| `bungee.Listeners` | `bungee.listener.BungeeListeners` | 更名 + 归位 |
| `bungee.BungeeCommunication` | `bungee.net.BungeeCommunication` | 归入 net 包 |
| `velocity.Listeners` | `velocity.listener.VelocityListeners` | 更名 + 归位 |
| `velocity.VelocityCommunication` | `velocity.net.VelocityCommunication` | 归入 net 包 |
| —（新增） | `bukkit.command.AbstractCommandSupport` | 命令基类 |
| —（新增） | `common.proxy.ProxyLoginTracker` | 代理端登录态追踪组件 |

## 重复点清单与消除方式

| 重复点 | 消除方式 |
| --- | --- |
| SQLite/MySQL 的连接校验/重建骨架 | 上提至 `SQL` 基类，子类仅实现 `createConnection()` |
| 5 个命令类的「非玩家守卫 + Floodgate 跳过」样板 | `AbstractCommandSupport` 基类统一 |
| bungee/velocity 登录态列表管理 | `ProxyLoginTracker` 组件 |
| 洪水门跳过判断表达式 ×5 | `LoginPlayerHelper.isBedrockLoginBypassed` |
| 口令变更-持久化块 ×3 | `LoginPlayerHelper.changePasswordAndPersist` |
| 邮件发送+成功/失败通知 + 验证码时长常量 ×2 | `EmailSender.sendEmailAsync` + `EmailCode.DEFAULT_CODE_DURATION` |

## 验证结果
- 每阶段 `mvn -o compile` 均 BUILD SUCCESS。
- 本次重构不改变任何运行逻辑（纯移动/抽取），入口类 FQCN 保持不变。
- 未引入 JUnit（按约定暂不加测试框架）。