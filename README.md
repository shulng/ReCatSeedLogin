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
- 🎒 **背包保护** - 登录前隐藏背包，防止物品丢失
- 📍 **位置保护** - 登录前强制传送至安全出生点
- 🕐 **重入限制** - 下线后可配置秒内禁止重新进入服务器
- 🌐 **IP限制** - 限制同IP账号注册/登录数量

### 📧 邮箱功能
- 📨 **邮箱绑定** - 支持邮箱验证与绑定
- 🔑 **密码重置** - 通过邮箱验证码重置密码
- 📤 **邮件通知** - 完整的邮件系统支持

### 🌐 代理端支持
- 🔄 **子服同步** - 支持BungeeCord和Velocity跨服登录
- 🚪 **子服限制** - 未登录禁止切换子服
- 🔄 **状态保持** - 登录后子服切换保持登录状态

### 💾 数据存储
- 🗄️ **多数据库支持** - SQLite(默认) / MySQL
- 📍 **玩家位置存储** - 玩家离线位置存储在数据库中
- 💾 **轻量级** - 占用资源少，性能优异

### 🌍 国际化支持
- 🌐 **多语言支持** - 内置中文、英文语言
- 🔧 **自定义语言** - 支持自定义语言文件覆盖
- 📝 **统一消息管理** - 使用MessageKey枚举统一管理所有消息

## 🏗️ 项目架构

插件采用多模块Maven架构，4个模块各司其职：

```
CatSeedLogin-v2/
├── common/       → 共享代码（API接口、配置管理、国际化、数据库抽象）
├── bukkit/       → Bukkit/Spigot/Paper/Folia 服务端插件（主插件）
├── bungeecord/   → BungeeCord 代理端插件
└── velocity/     → Velocity 代理端插件
```

| 模块 | 说明 | 输出JAR |
|------|------|---------|
| common | 共享API、配置、i18n、工具类 | CatSeedLogin-common.jar |
| bukkit | 服务端插件（注册/登录/管理等核心功能） | CatSeedLogin.jar |
| bungeecord | BungeeCord代理端（跨服登录状态同步） | CatSeedLogin-bungeecord.jar |
| velocity | Velocity代理端（跨服登录状态同步） | CatSeedLogin-velocity.jar |

### 架构说明
- **common模块** - 提供 `PlatformAdapter`、`CoreConfig` 等平台无关接口，以及配置管理、国际化、加密工具等
- **bukkit模块** - 基于Paper API（兼容Bukkit/Spigot/Paper/Folia），实现所有服务端核心功能
- **bungeecord模块** - 基于BungeeCord API，通过Socket与Bukkit端通信实现跨服登录状态同步
- **velocity模块** - 基于Velocity API，通过Socket与Bukkit端通信实现跨服登录状态同步

## 📥 下载安装

### 📦 下载地址
| 版本类型 | 下载链接 |
|---------|----------|
| 🔥 **最新稳定版** | [GitHub Releases](https://github.com/shulng/CatSeedLogin-v2/releases/latest) |
| 🔄 **自动构建版** | [GitHub Actions](https://github.com/shulng/CatSeedLogin-v2/actions/workflows/maven.yml) |

### 🚀 安装步骤

#### 单服务器使用
1. 下载 `CatSeedLogin.jar`
2. 将插件放入 `plugins` 文件夹
3. 重启服务器
4. 完成安装

#### BungeeCord/Velocity网络使用
1. **登录服务器** - 将 `CatSeedLogin.jar` 放入登录服务器的 `plugins` 文件夹
2. **代理端** - 将对应的代理端插件放入BungeeCord或Velocity的 `plugins` 文件夹
   - BungeeCord: `CatSeedLogin-bungeecord.jar`
   - Velocity: `CatSeedLogin-velocity.jar`
3. 配置代理端设置（详见[代理端配置](#-代理端配置)）
4. 重启所有相关服务

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

# 忘记密码重置
/resetpassword forget
/resetpassword re <验证码> <新密码>
```

## 📖 指令大全

### 🛠️ 管理员指令（Bukkit端）

| 指令 | 功能描述 |
|------|----------|
| `/catseedlogin reload` | 重载配置文件 |
| `/catseedlogin delPlayer <玩家名>` | 强制删除玩家账户 |
| `/catseedlogin setPwd <玩家名> <密码>` | 强制设置玩家密码 |

### ⚙️ 配置管理指令

| 指令 | 功能描述 | 默认值 |
|------|----------|--------|
| `/catseedlogin commandWhiteListAdd <指令>` | 添加登录前允许执行的指令 | 支持正则 |
| `/catseedlogin commandWhiteListDel <指令>` | 删除登录前允许执行的指令 | 支持正则 |
| `/catseedlogin commandWhiteListInfo` | 查看登录前允许执行的指令列表 | - |
| `/catseedlogin setIpRegCountLimit <数量>` | 设置同IP注册数量限制 | 2 |
| `/catseedlogin setIpCountLimit <数量>` | 设置同IP登录数量限制 | 2 |
| `/catseedlogin setIdLength <最短> <最长>` | 设置游戏名长度限制 | 2-15 |
| `/catseedlogin setReenterInterval <tick>` | 设置重入间隔限制 | 60tick(3秒) |
| `/catseedlogin setSpawnLocation` | 设置当前位置为登录出生点 | - |
| `/catseedlogin setAutoKick <秒数>` | 设置自动踢出时间 | 120秒 |
| `/catseedlogin limitChineseID` | 切换中文ID限制开关 | 开启 |
| `/catseedlogin beforeLoginNoDamage` | 切换登录前免伤开关 | 开启 |
| `/catseedlogin afterLoginBack` | 切换登录后返回开关 | 开启 |
| `/catseedlogin canTpSpawnLocation` | 切换强制登录点开关 | 开启 |
| `/catseedlogin deathStateQuitRecordLocation` | 切换死亡记录位置开关 | 开启 |

## 🔐 权限节点

| 权限节点 | 描述 |
|----------|------|
| `catseedlogin.command.catseedlogin` | 管理员指令使用权限 |

## ⚙️ 配置文件

### Bukkit端 config.yml
```yaml
# 语言设置 (zh_CN, en_US)
language: "zh_CN"

# 核心设置
settings:
  ip-register-count-limit: 2
  ip-count-limit: 2
  limit-chinese-id: true
  min-length-id: 2
  max-length-id: 15
  before-login-no-damage: true
  reenter-interval: 60
  after-login-back: true
  can-tp-spawn-location: true
  auto-kick: 120
  death-state-quit-record-location: true
  name-pattern: "^\\w+$"
  command-white-list:
    - "/(?i)l(ogin)?(\\z| .*)"
    - "/(?i)reg(ister)?(\\z| .*)"
    - "/(?i)resetpassword?(\\z| .*)"
    - "/(?i)repw?(\\z| .*)"

# 基岩版设置
bedrock:
  login-bypass: true
  floodgate-prefix-protect: true

# 同IP免登录设置
same-ip-login:
  enabled: false
  timeout: 5

# 登录前隐藏背包 (需要ProtocolLib)
empty-backpack: true

# 登录点设置
spawn:
  location: "world:0:64:0:0:0"

# 数据库设置
database:
  mysql: false
  host: "127.0.0.1"
  port: 3306
  database: "catseedlogin"
  user: "root"
  password: "password"

# 邮箱验证设置
email:
  enabled: false
  account: ""
  password: ""
  smtp-host: "smtp.example.com"
  smtp-port: "465"
  ssl-auth: true
  from-name: "Server"

# BungeeCord/Velocity 代理设置
proxy:
  enabled: false
  host: "127.0.0.1"
  port: 2333
  auth-key: ""
  login-server-name: "lobby"
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
│   BungeeCord    │ ← 安装代理端插件 + 配置config.yml
│   / Velocity    │
├─────────────────┤
│   登录服务器    │ ← 安装Bukkit端插件 + 配置config.yml
├─────────────────┤
│   游戏服务器1   │ ← 无需安装
│   游戏服务器2   │ ← 无需安装
└─────────────────┘
```

### 📋 登录服务器（Bukkit端）config.yml 中的 proxy 配置
```yaml
proxy:
  enabled: true                    # 启用代理模式
  host: 127.0.0.1                  # 通讯IP地址(建议使用内网)
  port: 2333                       # 通讯端口
  auth-key: "your-secret-key"      # 验证密钥
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
| Velocity | `/cslv status` | 查看插件状态 |
| Velocity | `/cslv list` | 查看已登录玩家列表 |

## 👨‍💻 开发者API

### 🎯 事件监听
- `CatSeedPlayerLoginEvent` - 玩家登录事件（Bukkit端）
- `CatSeedPlayerRegisterEvent` - 玩家注册事件（Bukkit端）

### 🔌 API接口
- `CatSeedLoginAPI` - 主要API接口类

```java
// 判断玩家是否已登录
boolean isLogin = CatSeedLoginAPI.isLogin(playerName);

// 判断玩家是否已注册
boolean isRegister = CatSeedLoginAPI.isRegister(playerName);

// 获取玩家最后登录时间
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
