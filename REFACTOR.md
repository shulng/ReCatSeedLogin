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

### 阶段 2：类层次优化
（待实现）