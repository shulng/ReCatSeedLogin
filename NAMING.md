# 命名标准化规范 (NAMING)

本文档定义 CatSeedLogin 全项目统一的命名标准化规范，涵盖分类体系与命名规则。
任何新增/修改的代码元素均须遵循本文档。与代码重构见 `REFACTOR.md`。

## 1. 分类体系

项目采用「平台 → 功能域」两级分层分类，以既有包结构为载体，层级清晰、逻辑相关、易于横向扩展。

```
ReCatSeedLogin
├── common/     跨平台共享核心（平台无关，仅依赖 JDK）
├── bukkit/     Bukkit/Spigot/Paper/Folia 服务端实现
├── bungee/     BungeeCord 代理端实现
└── velocity/   Velocity 代理端实现
```

每个平台内按**功能域**组织为子包：

| 功能域 | 语义 | 包名 |
| --- | --- | --- |
| 指令 | 命令执行器 | `command` |
| 配置 | 配置项与配置管理 | `config` |
| 缓存 | 运行时内存缓存 | `cache` |
| 数据库 | 持久化存储 | `database` |
| 事件 | 自定义事件/事件监听 | `event` / `listener` |
| 通信 | Socket 跨服同步 | `communication` / `net` |
| 平台抽象 | 平台上下文 | `platform` |
| 调度 | 异步/定时调度 | `scheduler` / `task` |
| 模型 | 数据模型 | `model` |
| 工具 | 无状态工具类 | `util` |

**可扩展性**：新增平台/功能时，按同一「平台根包 + 功能域子包」规则落位；新增功能域时在上级包新增同名子包即可，无需改动既有结构。

## 2. 命名规则

### 2.1 通用约定

| 维度 | 规则 |
| --- | --- |
| 大小写 | 依元素类型而定（见下表），不混用 |
| 长度 | 不超过 50 字符；避免无意义缩写（`lbl`→`label`） |
| 特殊字符 | 仅允许 `A-Z a-z 0-9 _`；禁止空格、`-`、`.、`、中文及运算符 |
| 前缀/后缀 | 见下表约定；不使用匈牙利式前缀（`m_`、`str`） |

### 2.2 元素类型规则

| 元素 | 规范 | 示例 | 备注 |
| --- | --- | --- | --- |
| 类/接口 | `UpperCamelCase`，首字母缩写全大写 | `CommandCatSeedLogin`、`ProxyLoginTracker` | 唯一、语义清晰，跨平台同名类置于各自平台包 |
| 常量 | `SCREAMING_SNAKE`，可加语义前缀 | `DEFAULT_CODE_DURATION` | 归入 `ConfigConstants` / `MessageKey` 常量宿主 |
| 配置字段 | `lowerCamelCase` | `loginWithSameIp`、`sslAuthVerify`、`enable` | 嵌套静态字段随类命名空间命名 |
| 方法 | `lowerCamelCase`，动词开头 | `loginWithSameIp()`、`changePasswordAndPersist()` | 布尔返回用 `is/has/can` 前缀 |
| 变量/参数 | `lowerCamelCase` | `rawPwd`、`sender` | 无缩写歧义 |
| 包 | 全小写 | `cc.baka9.catseedlogin.bukkit.command` | 域名反写 |

### 2.3 前缀/后缀约定

| 场景 | 约定 | 示例 |
| --- | --- | --- |
| 命令执行器 | 后缀 `Command` | `CommandRegister`、`CommandLogin` |
| 事件监听器 | 后缀 `Listener` / 无（监听统一 `Listeners`→`XxxListener`） | `PlayerListener`、`VelocityListeners` |
| 数据访问对象 | 后缀 `DAO` / `Repository`（未来新增） | — |
| 配置访问器（接口+实现） | 接口后缀 `Config`/`Manager` 样式缩写保留 | `getIPTimeout`、`isLoginWithSameIP`（见 §3） |
| 常量宿主 | 后缀 `Constants` / `Key` | `ConfigConstants`、`MessageKey` |

## 3. 记录的例外

- `CoreConfig` / `BaseConfigManager` 的访问器（`getIPTimeout`、`isLoginWithSameIP`、`getMaxLengthID`、`getMinLengthID`、`isLimitChineseID`）为「接口+实现」一致约定的缩写（符合 Google Java Style 对缩写单词的宽松处理），作为记录在案的例外保留，不在当前阶段归一。
- 对外配置键（`config.yml`）与 `MessageKey` 文案键为插件对外契约，重命名需谨慎评估兼容性，默认不随内部字段更名。

## 4. 实施记录

| 提交 | 类别 | 变更 |
| --- | --- | --- |
| 5A | 类/接口 | 命令类跨平台统一 `Command<领域>`；`PluginContext`(bukkit)→`BukkitContext` |
| 5B | 常量 | 审计 `ConfigConstants`/`MessageKey`：已全部合规，零改动 |
| 5C | 配置字段/方法 | `Config.java` 嵌套字段全部 `lowerCamelCase` 化；方法 `LoginwiththesameIP`→`loginWithSameIp` |
| 5D | 文档/收尾 | 新增 `NAMING.md`；更新 `REFACTOR.md`、`README.md` |