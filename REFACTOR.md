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

### 阶段 4：文档与收尾
（待实现）