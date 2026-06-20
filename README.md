# 🐱 CatSeedLogin - 猫种子登录系统

<div align="center">

[![Build Status](https://github.com/shulng/CatSeedLogin-v2/actions/workflows/maven.yml/badge.svg)](https://github.com/shulng/CatSeedLogin-v2/actions/workflows/maven.yml)
[![Release](https://img.shields.io/github/v/release/shulng/CatSeedLogin-v2)](https://github.com/shulng/CatSeedLogin-v2/releases/latest)
[![License](https://img.shields.io/github/license/shulng/CatSeedLogin-v2)](LICENSE)
[![Downloads](https://img.shields.io/github/downloads/shulng/CatSeedLogin-v2/total)](https://github.com/shulng/CatSeedLogin-v2/releases)

**🚀 高性能Minecraft登录插件 | 支持Bukkit/Spigot/Paper/Folia/BungeeCord/Velocity**

> 基于Paper API开发，支持Bukkit/Spigot/Paper/Folia服务端，以及BungeeCord和Velocity代理端

</div>

## 📋 目录

- [✨ 核心功能](#-核心功能)
- [🏗️ 项目架构](#️-项目架构)
- [📥 下载安装](#-下载安装)
- [🎯 快速开始](#-快速开始)
- [📖 指令大全](#-指令大全)
- [🔐 权限节点](#-权限节点)
- [⚙️ 配置文件](#️-配置文件)
- [🔗 代理端配置](#-代理端配置)
- [👨‍💻 开发者API](#开发者api)
- [💬 社区支持](#-社区支持)

## ✨ 核心功能

### 🔐 安全认证
- ✅ **注册/登录/修改密码** - 完整的用户认证系统
- ✅ **密码加密存储** - 采用Crypt加密算法，保障数据安全
- ✅ **防止大小写登录Bug** - 解决英文ID大小写敏感问题
- ✅ **防止账号被顶替** - 登录后防止他人顶号下线

### 🛡️ 安全防护
- 🔒 **登录前限制** - 禁止移动、交互、攻击、发言、使用指令等
- 🎒 **背包保护** - 登录前隐藏背包，防止物品丢失（需要ProtocolLib）
- 📍 **位置保护** - 登录前强制传送至安全出生点
- 🕐 **重入限制** - 下线后可配置tick内禁止重新进入服务器
- 🌐 **IP限制** - 限制同IP账号注册/登录数量
- 📝 **指令白名单** - 登录前仅允许执行白名单内的指令（支持正则表达式）

### 🔄 同IP免登录
- 🌐 **同IP跳过登录** - 同一IP在指定时间内重新登录可跳过密码验证
- ⏱️ **超时控制** - 可配置IP免登录的超时时间（分钟）

### 📧 邮箱功能
- 📨 **邮箱绑定** - 支持邮箱验证与绑定（两步验证：设置+验证码确认）
- 🔑 **密码重置** - 通过已绑定邮箱接收验证码重置密码
- 📤 **邮件通知** - 支持SSL/TLS两种邮件发送模式

### 🌐 代理端支持
- 🔄 **子服同步** - 支持BungeeCord和Velocity跨服登录状态同步
- 🚪 **子服限制** - 未登录禁止切换子服
- 🔄 **状态保持** - 登录后子服切换保持登录状态（KeepLoggedIn机制）
- 🔒 **防顶号** - 代理端检测重复登录，阻止同一账号多处同时在线

### 🎮 基岩版支持
- 📱 **登录绕过** - 基岩版(Floodgate)玩家可跳过登录验证
- 🛡️ **前缀保护** - 防止Java玩家使用基岩版名称前缀冒充基岩玩家

### 💾 数据存储
- 🗄️ **多数据库支持** - SQLite(默认) / MySQL
- 📍 **玩家位置存储** - 玩家离线位置存储在数据库中，登录后自动返回
- 💾 **轻量级** - 占用资源少，性能优异

### 🌍 国际化支持
- 🌐 **多语言支持** - 内置中文、英文语言
- 🔧 **自定义语言** - 支持自定义语言文件覆盖
- 📝 **统一消息管理** - 使用MessageKey枚举统一管理所有消息

### 🏗️ 兼容性
- 🌿 **Folia支持** - 通过CatScheduler实现Folia异步调度兼容
- 📦 **Paper支持** - 基于Paper API开发，完整兼容Paper系列服务端

## 🏗️ 项目架构

插件采用单模块Maven架构，编译为一个JAR包，同时支持Bukkit/BungeeCord/Velocity三个平台：

```
CatSeedLogin-v2/
├── pom.xml                      (Maven构建配置，输出单个Shaded JAR)
├── src/main/java/cc/baka9/catseedlogin/
│   ├── bukkit/                  → Bukkit/Spigot/Paper/Folia 服务端实现
│   │   ├── CatSeedLogin.java    → 服务端插件主类
│   │   ├── CatSeedLoginAPI.java → 开发者API
│   │   ├── command/             → 指令实现（登录/注册/改密/邮箱/管理）
│   │   ├── config/              → Bukkit配置管理
│   │   ├── database/            → 数据库实现（SQLite/MySQL）
│   │   ├── event/               → 自定义事件
│   │   ├── object/              → 登录状态管理、邮箱验证码
│   │   ├── task/                → 定时任务（自动踢出、登录提示）
│   │   └── util/                → 邮件发送工具
│   ├── bungee/                  → BungeeCord 代理端实现
│   │   ├── PluginMain.java      → BungeeCord插件主类
│   │   ├── BungeeCommunication.java → Socket通信客户端
│   │   ├── BungeeCommands.java  → 代理端指令
│   │   ├── Listeners.java       → 代理端事件监听
│   │   └── config/              → BungeeCord配置管理
│   ├── velocity/                → Velocity 代理端实现
│   │   ├── PluginMain.java      → Velocity插件主类
│   │   ├── VelocityCommunication.java → Socket通信客户端
│   │   ├── Commands.java        → 代理端指令
│   │   ├── Listeners.java       → 代理端事件监听
│   │   └── config/              → Velocity配置管理
│   └── common/                  → 跨平台共享代码
│       ├── api/                 → 平台抽象接口（PlatformAdapter、配置接口）
│       ├── communication/       → Socket通信基类
│       ├── config/              → 配置管理基类、YAML解析
│       ├── database/            → 数据库连接抽象
│       ├── i18n/                → 国际化引擎（I18n、MessageKey）
│       ├── model/               → 数据模型（LoginPlayer）
│       └── util/                → 加密、验证、日期工具类
└── src/main/resources/
    ├── plugin.yml               → Bukkit插件描述文件
    ├── bungee.yml               → BungeeCord插件描述文件
    ├── velocity-plugin.json     → Velocity插件描述文件
    ├── config.yml               → 统一配置文件（所有平台共享）
    └── languages/               → 语言文件（zh-CN.yml、en-US.yml）
```

### 架构说明
- **单JAR部署** - 所有平台代码打包为一个Shaded JAR，每个平台自动加载对应的入口类
- **Bukkit端** - 服务端插件，实现注册/登录/管理等核心功能，同时运行Socket服务器供代理端查询登录状态
- **BungeeCord端** - 代理端插件，通过TCP Socket与Bukkit端通信，实现跨服登录状态同步
- **Velocity端** - 代理端插件，通过TCP Socket与Bukkit端通信，实现跨服登录状态同步
- **common包** - 跨平台共享代码，提供 `PlatformAdapter`、`CoreConfig` 等平台无关接口

## 📥 下载安装

### 📦 下载地址
| 版本类型 | 下载链接 |
|---------|----------|
| 🔥 **最新稳定版** | [GitHub Releases](https://github.com/shulng/CatSeedLogin-v2/releases/latest) |
| 🔄 **自动构建版** | [GitHub Actions](https://github.com/shulng/CatSeedLogin-v2/actions/workflows/maven.yml) |

### 🚀 安装步骤

#### 单服务器使用
1. 下载 `CatSeedLogin-<版本>.jar`
2. 将插件放入 `plugins` 文件夹
3. 重启服务器
4. 完成安装

#### BungeeCord/Velocity网络使用
1. **登录服务器** - 将 `CatSeedLogin-<版本>.jar` 放入登录服务器的 `plugins` 文件夹
2. **代理端** - 将**同一个** `CatSeedLogin-<版本>.jar` 放入BungeeCord或Velocity的 `plugins` 文件夹
3. 配置代理端设置（详见[代理端配置](#-代理端配置)）
4. 重启所有相关服务

> **注意**: Bukkit端和代理端使用的是同一个JAR包，插件会根据运行环境自动加载对应平台的实现。

### 📋 系统要求
- **Java**: 8+
- **服务端**: Bukkit/Spigot/Paper/Folia（API版本1.13+）
- **代理端**: BungeeCord 或 Velocity

## 🎯 快速开始

### 🔑 玩家指令

#### 注册账号
```
/register <密码> <重复密码>
/reg <密码> <重复密码>
```

#### 登录账号
```
/login <密码>
/l <密码>
```

#### 修改密码
```
/changepassword <旧密码> <新密码> <重复新密码>
/changepw <旧密码> <新密码> <重复新密码>
```

#### 邮箱相关
```
# 绑定邮箱
/bindemail set <邮箱地址>
/bindemail verify <验证码>

# 忘记密码重置（需要先绑定邮箱）
/resetpassword forget
/resetpassword re <验证码> <新密码>
```

### 📋 密码规则
- 长度：6-16位
- 必须同时包含字母和数字
- 不允许纯数字或纯字母

## 📖 指令大全

### 🛠️ 管理员指令（Bukkit端，别名 `/cslogin`）

| 指令 | 功能描述 |
|------|----------|
| `/catseedlogin reload` | 重载配置文件 |
| `/catseedlogin delPlayer <玩家名>` | 强制删除玩家账户 |
| `/catseedlogin setPwd <玩家名> <密码>` | 强制设置玩家密码 |

### ⚙️ 配置管理指令

| 指令 | 功能描述 | 默认值 |
|------|----------|--------|
| `/catseedlogin commandWhiteListAdd <正则>` | 添加登录前允许执行的指令（支持正则） | - |
| `/catseedlogin commandWhiteListDel <正则>` | 删除登录前允许执行的指令 | - |
| `/catseedlogin commandWhiteListInfo` | 查看登录前允许执行的指令列表 | - |
| `/catseedlogin setIpRegCountLimit <数量>` | 设置同IP注册数量限制 | 2 |
| `/catseedlogin setIpCountLimit <数量>` | 设置同IP登录数量限制 | 2 |
| `/catseedlogin setIdLength <最短> <最长>` | 设置游戏名长度限制 | 2-15 |
| `/catseedlogin setReenterInterval <tick>` | 设置重入间隔限制 | 60tick(3秒) |
| `/catseedlogin setSpawnLocation` | 设置当前位置为登录出生点 | - |
| `/catseedlogin setAutoKick <秒数>` | 设置自动踢出时间（<=0关闭） | 120秒 |

### 🔀 开关切换指令

| 指令 | 功能描述 | 默认值 |
|------|----------|--------|
| `/catseedlogin limitChineseID` | 切换中文ID限制开关 | 开启 |
| `/catseedlogin bedrockLoginBypass` | 切换基岩版登录绕过开关 | 开启 |
| `/catseedlogin LoginwiththesameIP` | 切换同IP免登录开关 | 关闭 |
| `/catseedlogin beforeLoginNoDamage` | 切换登录前免伤开关 | 开启 |
| `/catseedlogin afterLoginBack` | 切换登录后返回开关 | 开启 |
| `/catseedlogin canTpSpawnLocation` | 切换强制登录点开关 | 开启 |
| `/catseedlogin deathStateQuitRecordLocation` | 切换死亡记录位置开关 | 开启 |

## 🔐 权限节点

| 权限节点 | 平台 | 描述 |
|----------|------|------|
| `catseedlogin.command.catseedlogin` | Bukkit | 管理员指令使用权限 |
| `catseedlogin.admin` | BungeeCord | BungeeCord端管理员指令权限 |
| `catseedlogin.admin` | Velocity | Velocity端管理员指令权限 |

> 玩家指令（`/login`、`/register`、`/changepassword`、`/bindemail`、`/resetpassword`）不需要权限节点，所有玩家均可使用。

## ⚙️ 配置文件

### config.yml（所有平台共享）
```yaml
# 语言设置 (zh_CN, en_US)
language: "zh_CN"

# 核心设置
settings:
  ip-register-count-limit: 2       # 同IP注册数量限制
  ip-count-limit: 2                # 同IP登录数量限制
  limit-chinese-id: true           # 是否限制中文ID
  min-length-id: 2                 # 游戏ID最小长度
  max-length-id: 15                # 游戏ID最大长度
  before-login-no-damage: true     # 登录前不受到伤害
  reenter-interval: 60             # 重入间隔限制 (tick, 20tick=1秒)
  after-login-back: true           # 登录后是否返回退出地点
  can-tp-spawn-location: true      # 登录前是否强制在登录地点
  auto-kick: 120                   # 自动踢出未登录的玩家 (秒, <=0关闭)
  death-state-quit-record-location: true  # 死亡状态退出是否记录位置
  name-pattern: "^\\w+$"           # 游戏名正则表达式
  command-white-list:              # 登录前允许执行的指令 (支持正则)
    - "/(?i)l(ogin)?(\\z| .*)"
    - "/(?i)reg(ister)?(\\z| .*)"
    - "/(?i)resetpassword?(\\z| .*)"
    - "/(?i)repw?(\\z| .*)"

# 基岩版设置
bedrock:
  login-bypass: true               # 基岩版(Floodgate)玩家跳过登录
  floodgate-prefix-protect: true   # 防止Java玩家使用基岩版名称前缀

# 同IP免登录设置
same-ip-login:
  enabled: false                   # 是否启用同IP免登录
  timeout: 5                       # IP免登录超时时间 (分钟)

# 登录前隐藏背包 (需要ProtocolLib)
empty-backpack: true

# 登录点设置
spawn:
  location: ""                     # 格式: 世界名:x:y:z:yaw:pitch (留空自动使用服务器出生点)

# 数据库设置
database:
  mysql: false                     # 使用MySQL (false=SQLite)
  host: "127.0.0.1"
  port: 3306
  database: "catseedlogin"
  user: "root"
  password: "password"

# 邮箱验证设置
email:
  enabled: false                   # 是否启用邮箱功能
  account: ""                      # 邮箱账号
  password: ""                     # 邮箱密码
  smtp-host: "smtp.example.com"    # SMTP服务器
  smtp-port: "465"                 # SMTP端口
  ssl-auth: true                   # SSL认证 (true=SSL端口465, false=STARTTLS端口587)
  from-name: "Server"              # 发件人显示名称

# BungeeCord/Velocity 代理设置
proxy:
  enabled: false                   # 启用代理模式 (Bukkit端开启Socket服务器)
  host: "127.0.0.1"                # 通讯IP地址(建议使用内网)
  port: 2333                       # 通讯端口
  auth-key: ""                     # 验证密钥 (用于KeepLoggedIn签名验证)
  login-server-name: "lobby"       # 登录服务器名称(与代理端配置一致)
```

### 语言文件
语言文件存放在 `plugins/CatSeedLogin/languages/` 文件夹中：
- 中文：`zh-CN.yml`
- 英文：`en-US.yml`

配置文件中的 `language` 选项使用下划线格式（如 `zh_CN`），对应语言文件使用标准格式（如 `zh-CN.yml`）。支持自定义语言覆盖。

## 🔗 代理端配置

### 🏗️ 网络架构
```
┌─────────────────┐
│   BungeeCord    │ ← 安装同一个CatSeedLogin JAR
│   / Velocity    │
├─────────────────┤
│   登录服务器    │ ← 安装CatSeedLogin JAR + 启用proxy.enabled
├─────────────────┤
│   游戏服务器1   │ ← 无需安装
│   游戏服务器2   │ ← 无需安装
└─────────────────┘

通信方式: TCP Socket (端口 2333)
Bukkit端运行Socket服务器，代理端通过Socket查询登录状态
```

### 📋 登录服务器（Bukkit端）config.yml 中的 proxy 配置
```yaml
proxy:
  enabled: true                    # 启用代理模式（开启Socket服务器）
  host: 127.0.0.1                  # 通讯IP地址(建议使用内网)
  port: 2333                       # 通讯端口
  auth-key: "your-secret-key"      # 验证密钥（务必设置）
  login-server-name: "login"       # 登录服务器名称(与代理端一致)
```

### 🔗 BungeeCord端 config.yml
```yaml
proxy:
  host: 127.0.0.1                  # 通讯IP地址(需与Bukkit端一致)
  port: 2333                       # 通讯端口(需与Bukkit端一致)
  auth-key: "your-secret-key"      # 验证密钥(需与Bukkit端一致)
  login-server-name: "login"       # 登录服务器名称(在BungeeCord中配置的服务器名)
```

### ⚡ Velocity端 config.yml
```yaml
proxy:
  host: 127.0.0.1                  # 通讯IP地址(需与Bukkit端一致)
  port: 2333                       # 通讯端口(需与Bukkit端一致)
  auth-key: "your-secret-key"      # 验证密钥(需与Bukkit端一致)
  login-server-name: "login"       # 登录服务器名称(在Velocity中配置的服务器名)
```

### 🔄 代理端指令
| 平台 | 指令 | 功能 |
|------|------|------|
| BungeeCord | `/cslb reload` | 重载BungeeCord端配置 |
| Velocity | `/cslv reload` | 重载Velocity端配置 |
| Velocity | `/cslv status` | 查看插件状态（通讯地址、在线状态） |
| Velocity | `/cslv list` | 查看已登录玩家列表 |

### 🔄 代理端工作流程
1. 玩家加入网络 → 连接到登录服务器
2. 玩家执行 `/login` 或 `/register` → Bukkit端处理认证
3. 认证成功 → 玩家可自由切换子服
4. 切换子服时 → 代理端通过Socket验证Bukkit端的登录状态
5. 未登录玩家 → 被重定向回登录服务器

## 👨‍💻 开发者API

### 🎯 事件监听
- `CatSeedPlayerLoginEvent` - 玩家登录事件（包含登录结果：成功/失败）
- `CatSeedPlayerRegisterEvent` - 玩家注册事件

```java
// 监听登录事件
@EventHandler
public void onLogin(CatSeedPlayerLoginEvent event) {
    Player player = event.getPlayer();
    CatSeedPlayerLoginEvent.Result result = event.getResult();
    Optional<String> email = event.getEmail();
    // ...
}

// 监听注册事件
@EventHandler
public void onRegister(CatSeedPlayerRegisterEvent event) {
    Player player = event.getPlayer();
    // ...
}
```

### 🔌 API接口
```java
import cc.baka9.catseedlogin.bukkit.CatSeedLoginAPI;

// 判断玩家是否已登录
boolean isLogin = CatSeedLoginAPI.isLogin(playerName);

// 判断玩家是否已注册
boolean isRegister = CatSeedLoginAPI.isRegister(playerName);

// 获取玩家最后登录时间（返回epoch毫秒，未注册返回null）
Long lastLogin = CatSeedLoginAPI.getLastLoginTime(playerName);
```

## 💬 社区支持

### 🏘️ 交流群组
[![QQ交流群](https://img.shields.io/badge/QQ%E4%BA%A4%E6%B5%81%E7%BE%A4-839815243-blue?style=flat-square&logo=tencent-qq)](http://shang.qq.com/wpa/qunwpa?idkey=91199801a9406f659c7add6fb87b03ca071b199b36687c62a3ac51bec2f258a3)

### 📊 项目统计
- ⭐ **给项目点星**：[GitHub仓库](https://github.com/shulng/CatSeedLogin-v2)
- 🐛 **提交Issue**：[问题反馈](https://github.com/shulng/CatSeedLogin-v2/issues)
- 📖 **贡献代码**：[Pull Request](https://github.com/shulng/CatSeedLogin-v2/pulls)

---

<div align="center">

**Made with ❤️ by [CatSeed](https://github.com/CatSeed) & [shulng](https://github.com/shulng)**

*如果这个插件对你有帮助，欢迎给项目点个 ⭐ 支持一下！*

</div>
