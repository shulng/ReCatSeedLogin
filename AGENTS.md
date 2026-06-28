# AGENTS.md

## Build & Verify

```bash
# Build (produces shaded JAR in target/)
mvn -B clean package --no-transfer-progress

# Build with verbose output
mvn -B clean package
```

No lint/typecheck/test commands exist. The repo has no test source (`src/test/` is gitignored). CI only runs `mvn clean package`.

## Architecture

Single-module Maven project. Three platform entry points and shared common code coexist in one module under `src/main/java/cc/baka9/catseedlogin/`:

```
CatSeedLogin-v2/
├── pom.xml                  (packaging=jar, single shaded JAR)
└── src/main/java/cc/baka9/catseedlogin/
    ├── common/              (shared code across platforms)
    │   ├── api/             → PlatformAdapter, CoreConfig, DatabaseConfig, EmailConfig, BungeeCordConfig
    │   ├── communication/   → BaseCommunication
    │   ├── config/          → BaseConfigManager, YamlConfiguration, ConfigConstants, ConfigHelper
    │   ├── i18n/            → I18n, MessageKey
    │   ├── model/           → LoginPlayer
    │   └── util/            → Crypt, ValidationUtil, CommunicationAuth, DateUtil
    ├── bukkit/              (Bukkit platform code)
    │   ├── CatSeedLogin.java       → Bukkit plugin main class (JavaPlugin)
    │   ├── command/                → CommandLogin, CommandRegister, CommandChangePassword, etc.
    │   ├── config/                 → BukkitConfigManager, BukkitPlatformAdapter
    │   ├── database/               → SQL, SQLite, MySQL, BufferStatement
    │   ├── event/                  → CatSeedPlayerLoginEvent, CatSeedPlayerRegisterEvent
    │   ├── object/                 → LoginPlayerHelper, EmailCode
    │   ├── task/                   → Task, TaskAutoKick, TaskSendLoginMessage
    │   └── util/                   → EmailSender
    ├── bungee/              (BungeeCord platform code)
    │   ├── PluginMain.java         → BungeeCord plugin main class
    │   ├── config/                 → BungeeConfigManager, BungeePlatformAdapter
    │   ├── BungeeCommunication.java
    │   ├── BungeeCommands.java
    │   └── Listeners.java
    └── velocity/            (Velocity platform code)
        ├── PluginMain.java         → Velocity plugin main class
        ├── config/                 → VelocityConfigManager, VelocityPlatformAdapter
        ├── VelocityCommunication.java
        ├── Commands.java
        └── Listeners.java
```

**Entry points** (each platform's `onEnable`/initialization):
- `bukkit/CatSeedLogin.java` — Bukkit plugin main class (`JavaPlugin`)
- `bungee/PluginMain.java` — BungeeCord plugin main class
- `velocity/PluginMain.java` — Velocity plugin main class

**API interfaces** (`common/api/`):
- `PlatformAdapter` — platform abstraction (logging, scheduling, player operations)
- `CoreConfig`, `DatabaseConfig`, `EmailConfig`, `BungeeCordConfig` — config interfaces

**Config flow**: Each platform has its own `ConfigManager` (e.g., `BukkitConfigManager`) extending `BaseConfigManager`. All platforms read from a single `config.yml` at runtime.

**Database**: `bukkit/database/SQL.java` (abstract) → `SQLite.java` or `MySQL.java`. Uses raw JDBC, no ORM.

## Key Conventions

- **Java 8 target** (`maven.compiler.source/target=8`). Do not use Java 9+ APIs.
- **Lombok** is used (`provided` scope). Annotations like `@Getter`, `@Setter`, `@Data` are expected.
- **Version filtering**: `${version}` in parent `pom.xml` is filtered into `common/Version.java` and plugin descriptors (`plugin.yml`, `bungee.yml`, `velocity-plugin.json`).
- **Paper API** is the sole Bukkit-side API dependency (replaces separate bukkit, spigot-api, paper-api, folia-api dependencies). Folia support is detected at runtime via MorePaperLib.
- **i18n**: All user-facing strings go through `I18n` + `MessageKey` enum. Language files are `languages/zh-CN.yml` and `languages/en-US.yml`. The config key `language` uses underscore format (`zh_CN`) mapping to dash-format filenames (`zh-CN.yml`).
- **Dependencies compiled into JAR** (shaded): sqlite-jdbc, mysql-connector-java, snakeyaml, javax.mail, commons-email, commons-lang3, commons-net, FoliaLib, MorePaperLib.
- **Dependencies NOT shaded** (provided by server): paper-api, bungeecord-api, velocity-api, ProtocolLib, Lombok, Floodgate.

## Gotchas

- `plugin.yml` lists `main: cc.baka9.catseedlogin.bukkit.CatSeedLogin`. The Bukkit entry point is the only platform that registers database, commands, and event listeners directly.
- ProtocolLib integration is optional — detected at runtime via `Class.forName`. If absent, backpack hiding is disabled.
- BungeeCord/Velocity plugins communicate with the Bukkit plugin over a socket (see `Communication` / `BungeeCommunication` / `VelocityCommunication` on port 2333).
- The `proxy.enabled` config flag controls whether the Bukkit plugin opens a socket server for cross-proxy auth.
- `src/test/` is gitignored — no automated tests exist.
