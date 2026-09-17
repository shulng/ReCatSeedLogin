# CatSeedLogin 深度结构优化与重构方案

## Context（背景与目标）

CatSeedLogin 是一个同时支持 Bukkit / BungeeCord / Velocity 三平台的 Minecraft 登录插件，采用单 JAR 打包。当前代码存在以下问题：

1. **bukkit 根包混乱**：入口类、配置、缓存、调度器、监听器、通信、API 全部平铺在 `bukkit/` 根包，与已有子包（command/config/database...）职责混杂。
2. **命名冲突/混淆**：`common.config.PluginContext` 与 `bukkit.PluginContext` 同名不同职责；`common/config` 与 `bukkit/config`、`bukkit/Config` 职责重叠。
3. **重复代码**：
   - `SQLite` / `MySQL` 的连接有效性校验、连接创建/关闭骨架几乎一致（`isConnectionValid()` 完全重复）。
   - bukkit 5 个命令类重复「玩家守卫 + 洪水门(Floodgate)跳过 + 消息发送」样板。
   - 口令变更-持久化块（`updatePassword → sql.edit → Cache.refresh`）在 Login/ChangePassword/ResetPassword 三处重复。
   - 绑定邮箱与重置密码的邮件发送编排、验证码时长常量 `1000*60*5` 重复。
   - 洪水门跳过判断 `Config.Settings.BedrockLoginBypass && LoginPlayerHelper.isFloodgatePlayer(...)` 在 5 个命令类重复。
   - bungee / velocity 的登录态追踪列表（`CopyOnWriteArrayList` + add/contains/remove）逻辑相同，仅 API 不同。

**目标**：在不改变插件行为、不破坏三个平台入口的前提下，重组目录结构、优化类层次、系统性分类、提取复用组件，做到高内聚低耦合、可测试性更好，并分阶段提交 git、产出 REFACTOR.md。

**约束**：入口类 `bukkit.CatSeedLogin` / `bungee.PluginMain` / `velocity.PluginMain` 保持 FQCN 不变（plugin.yml / bungee.yml / velocity-plugin.json 已引用），避免改动描述文件。编译（`mvn -q -DskipTests package`）作为回归安全网。

---

## 目标目录结构

```
cc/baka9/catseedlogin/
├── common/                      # 跨平台共享核心
│   ├── api/        PlatformAdapter, CoreConfig, EmailConfig, DatabaseConfig, BungeeCordConfig
│   ├── communication/  BaseCommunication, ProxyLoginTracker(新增), ConnectionAuth
│   ├── config/     Configuration, ConfigurationSection, YamlConfiguration, BaseConfigManager, ConfigHelper, ConfigConstants
│   ├── i18n/       I18n, MessageKey (+ ResourceProvider)
│   ├── model/      LoginPlayer
│   ├── platform/   PluginContext        ← 由 common/config/PluginContext 迁入并改名，消除与 bukkit.PluginContext 混淆
│   └── util/       Crypt, DateUtil, PasswordHelper, ValidationUtil, CommunicationAuth, CommandUtil(新增)
├── bukkit/
│   ├── CatSeedLogin.java                  (入口，FQCN 不变)
│   ├── api/        CatSeedLoginAPI
│   ├── cache/      PlayerCache            ← 由 Cache 迁入
│   ├── command/    AbstractCommandSupport(新增基类) + 各命令
│   ├── communication/  Communication      ← 由根包迁入
│   ├── config/     Config(由根包迁入), BukkitConfigManager, BukkitPlatformAdapter
│   ├── database/   SQL, SQLite, MySQL, BufferStatement
│   ├── event/      CatSeedPlayerLoginEvent, CatSeedPlayerRegisterEvent
│   ├── listener/   PlayerListener(由 Listeners 迁入), ProtocolLibListener(由 ProtocolLibListeners 迁入)
│   ├── object/     LoginPlayerHelper, EmailCode
│   ├── platform/   PluginContext          ← 由根包迁入（与 common.platform.PluginContext 隔离）
│   ├── scheduler/  CatScheduler
│   ├── task/       Task, TaskAutoKick, TaskSendLoginMessage
│   └── util/       EmailSender, WorldUtil
├── bungee/
│   ├── PluginMain.java                    (入口，FQCN 不变)
│   ├── command/   BungeeCommands
│   ├── config/    BungeeConfigManager, BungeePlatformAdapter
│   ├── listener/  BungeeListeners         ← 由 Listeners 迁入
│   └── net/       BungeeCommunication     ← 由根包迁入
└── velocity/
    ├── PluginMain.java                    (入口，FQCN 不变)
    ├── command/   Commands
    ├── config/    VelocityConfigManager, VelocityPlatformAdapter
    ├── listener/  VelocityListeners       ← 由 Listeners 迁入
    └── net/       VelocityCommunication   ← 由根包迁入
```

> 说明：仅当有价值时才重命名类（Cache→PlayerCache、Listeners→PlayerListener/ProtocolLibListener、config/PluginContext→platform/PluginContext）；其余仅移动包。所有移动由 Maven 编译兜底，漏改引用会立即暴露。

---

## 分阶段实施（每阶段独立 git commit）

### 阶段 0：基线
- 确认当前可编译：`mvn -q -DskipTests package`。
- 确认 git 工作区干净，创建 `REFACTOR.md` 骨架。

### 阶段 1：目录重组（纯移动，改 package/import）
按上述目标结构移动文件并更新 `package` 与 `import`：
- 根 `bungee/velocity` 的 `Listeners` → `listener/`，`Communication`→`net/`。
- `bukkit` 根包类分派到 `cache/config/listener/platform/scheduler/communication/api`。
- `common/config/PluginContext` → `common/platform/PluginContext`（仅 bungee 侧 2 处 import 受影响，见 grep 证据）。
- 全量编译，修复遗漏引用。**Commit 1**.

### 阶段 2：类层次优化（基类/接口）
1. **SQL 基类**：把 `isConnectionValid()`、`closeConnection()` 骨架上提到 `SQL` 基类，新增抽象 `createConnection()`；`SQLite`/`MySQL` 只实现建连差异。消除两处重复。
2. **AbstractCommandSupport（bukkit 命令基类）**：封装重复样板——「非玩家/参数不足守卫」「Floodgate 跳过」「失效检查时统一返回」，让子类实现 `onPlayerCommand(player,args)`；避免强制模板方法，抽取到可在子类调用的受保护工具方法。
3. **ProxyLoginTracker（common/proxy）**：封装登录态列表与 `sendConnectRequest/sendKeepLoggedInRequest` 流程，bungee/velocity 监听器委托给该组件。**Commit 2**.

### 阶段 3：重复代码提取（复用组件）
统一收口到既有组件（LoginPlayerHelper / EmailCode / 新工具）：
1. **洪水门跳过**：`Config.Settings.BedrockLoginBypass && LoginPlayerHelper.isFloodgatePlayer(player)` → `LoginPlayerHelper.isBedrockLoginBypassed(player)`，替换 5 个命令类。
2. **口令变更持久化**：`PasswordHelper.updatePassword → PluginContext.getSql().edit → Cache.refresh` → `LoginPlayerHelper.changePasswordAndPersist(lp, rawPwd)`，替换 Login/ChangePassword/ResetPassword 三处。
3. **邮件发送编排**：新建 `common` 或 `bukkit/util` 的邮件发送辅助（受「用户已确认不加测试」约束，仅做纯函数化抽取），统一 ResetPassword/BindEmail 的 `sendEmail + notify 成功/失败` 与 `EMAIL_CODE_DURATION` 常量。
4. 顺带清理：命令类 `name = sender.getName()` / `player = (Player)sender` 等零散重复通过基类方法消解。**Commit 3**.

### 阶段 4：文档与收尾
- 补全 `REFACTOR.md`：记录重构前后结构对照、各阶段决策、重复点清单与消除方式、验证结果。
- 更新 `README.md` 的「项目架构」目录树为新的目标结构（如可行且低风险）。
- 最终 `mvn -q -DskipTests package` 通过。**Commit 4**.

---

## 验证方式
1. **编译/打包**：每个阶段后执行 `mvn -q -DskipTests package`，确保三平台全部编译并通过 shade 打包。
2. **入口/描述文件**：抽查确保 `plugin.yml` / `bungee.yml` / `velocity-plugin.json` 的 main 类 FQCN 未被改动且有效。
3. **行为等价**：本次重构不改变任何运行逻辑（纯移动/抽取），通过 diff 评审 + 编译验证保证契约不变。
4. 不引入 JUnit（用户已确认暂不加测试框架）。

## 关键文件（代表路径）
- 移动类：`bukkit/{Cache,Config,Listeners,PluginContext,CatScheduler,Communication,CatSeedLoginAPI,ProtocolLibListeners}.java`，`bungee/{Listeners,BungeeCommunication}`，`velocity/{Listeners,VelocityCommunication}`，`common/config/PluginContext.java`。
- 抽取点（编辑而非新增默认）：[LoginPlayerHelper.java](file:///c:/Users/shulng/IdeaProjects/ReCatSeedLogin/src/main/java/cc/baka9/catseedlogin/bukkit/object/LoginPlayerHelper.java)、[SQL.java](file:///c:/Users/shulng/IdeaProjects/ReCatSeedLogin/src/main/java/cc/baka9/catseedlogin/bukkit/database/SQL.java)、[SQLite.java](file:///c:/Users/shulng/IdeaProjects/ReCatSeedLogin/src/main/java/cc/baka9/catseedlogin/bukkit/database/SQLite.java)、[MySQL.java](file:///c:/Users/shulng/IdeaProjects/ReCatSeedLogin/src/main/java/cc/baka9/catseedlogin/bukkit/database/MySQL.java)。
- 命令类样板来源：[CommandLogin.java](file:///c:/Users/shulng/IdeaProjects/ReCatSeedLogin/src/main/java/cc/baka9/catseedlogin/bukkit/command/CommandLogin.java)、[CommandRegister.java](file:///c:/Users/shulng/IdeaProjects/ReCatSeedLogin/src/main/java/cc/baka9/catseedlogin/bukkit/command/CommandRegister.java)、[CommandChangePassword.java](file:///c:/Users/shulng/IdeaProjects/ReCatSeedLogin/src/main/java/cc/baka9/catseedlogin/bukkit/command/CommandChangePassword.java)、[CommandResetPassword.java](file:///c:/Users/shulng/IdeaProjects/ReCatSeedLogin/src/main/java/cc/baka9/catseedlogin/bukkit/command/CommandResetPassword.java)、[CommandBindEmail.java](file:///c:/Users/shulng/IdeaProjects/ReCatSeedLogin/src/main/java/cc/baka9/catseedlogin/bukkit/command/CommandBindEmail.java)。