package cc.baka9.catseedlogin.common.config

import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class ConfigConstants private constructor() {
    companion object {
        @JvmField val DEFAULT_LANGUAGE = "zh_CN"
        @JvmField val DEFAULT_PROXY_HOST = "127.0.0.1"
        @JvmField val DEFAULT_PROXY_PORT = 2333
        @JvmField val DEFAULT_MYSQL_PORT = 3306
        @JvmField val DEFAULT_DATABASE_NAME = "catseedlogin"
        @JvmField val DEFAULT_SPAWN_LOCATION = ""
        @JvmField val DEFAULT_NAME_PATTERN = "^\\w+$"
        @JvmField val DEFAULT_IP_REGISTER_LIMIT = 2
        @JvmField val DEFAULT_IP_LOGIN_LIMIT = 2
        @JvmField val DEFAULT_MIN_NAME_LENGTH = 2
        @JvmField val DEFAULT_MAX_NAME_LENGTH = 15
        @JvmField val DEFAULT_AUTO_KICK_SECONDS = 120
        @JvmField val DEFAULT_REENTER_INTERVAL_TICKS = 60
        @JvmField val DEFAULT_IP_TIMEOUT_MINUTES = 5
        @JvmField val DEFAULT_TWO_FACTOR_ENABLED = false
        @JvmField val DEFAULT_TWO_FACTOR_ISSUER = "CatSeedLogin"
        @JvmField val DEFAULT_TWO_FACTOR_DIGITS = 6
        @JvmField val DEFAULT_TWO_FACTOR_PERIOD = 30
        @JvmField val DEFAULT_TWO_FACTOR_RECOVERY_CODE_COUNT = 10
        @JvmField val DEFAULT_TWO_FACTOR_MAX_ATTEMPTS = 5
        @JvmField val DEFAULT_TWO_FACTOR_ATTEMPT_WINDOW = 300
        @JvmField val DEFAULT_TWO_FACTOR_PENDING_TIMEOUT = 0
        @JvmField val DEFAULT_SMTP_HOST = "smtp.example.com"
        @JvmField val DEFAULT_SMTP_PORT = "465"
        @JvmField val DEFAULT_FROM_NAME = "Server"
        @JvmField val DEFAULT_COMMAND_WHITELIST: MutableList<String> = arrayListOf(
            "/(?i)l(ogin)?(\\z| .*)",
            "/(?i)reg(ister)?(\\z| .*)",
            "/(?i)resetpassword?(\\z| .*)",
            "/(?i)repw?(\\z| .*)"
        )

        @JvmStatic fun compilePatternOrDefault(pattern: String?, defaultPattern: String): Pattern {
            if (pattern.isNullOrEmpty()) return Pattern.compile(defaultPattern)
            return try { Pattern.compile(pattern) } catch (_: PatternSyntaxException) { Pattern.compile(defaultPattern) }
        }

        @JvmStatic fun compilePatterns(patterns: List<String>?): List<Pattern> {
            if (patterns.isNullOrEmpty()) return emptyList()
            return patterns.mapNotNull { try { Pattern.compile(it) } catch (_: PatternSyntaxException) { null } }
        }
    }

    class Path private constructor() {
        companion object {
            @JvmField val SETTINGS_ACCOUNT_LIMIT_CHINESE_ID = "settings.account.limit-chinese-id"
            @JvmField val SETTINGS_ACCOUNT_MIN_LENGTH_ID = "settings.account.min-length-id"
            @JvmField val SETTINGS_ACCOUNT_MAX_LENGTH_ID = "settings.account.max-length-id"
            @JvmField val SETTINGS_ACCOUNT_NAME_PATTERN = "settings.account.name-pattern"
            @JvmField val SETTINGS_BEFORE_LOGIN_NO_DAMAGE = "settings.before-login.no-damage"
            @JvmField val SETTINGS_BEFORE_LOGIN_CAN_TP_SPAWN = "settings.before-login.can-tp-spawn-location"
            @JvmField val SETTINGS_BEFORE_LOGIN_ALLOW_CHAT = "settings.before-login.allow-chat"
            @JvmField val SETTINGS_BEFORE_LOGIN_BLINDING = "settings.before-login.blinding"
            @JvmField val SETTINGS_BEFORE_LOGIN_EMPTY_BACKPACK = "settings.before-login.empty-backpack"
            @JvmField val SETTINGS_BEFORE_LOGIN_AUTO_KICK = "settings.before-login.auto-kick"
            @JvmField val SETTINGS_BEFORE_LOGIN_COMMAND_WHITELIST = "settings.before-login.command-white-list"
            @JvmField val SETTINGS_LOGIN_LOOPBACK_BYPASS = "settings.login.loopback-bypass"
            @JvmField val SETTINGS_LOGIN_REENTER_INTERVAL = "settings.login.reenter-interval"
            @JvmField val SETTINGS_LOGIN_AFTER_LOGIN_BACK = "settings.login.after-login-back"
            @JvmField val SETTINGS_LOGIN_DEATH_STATE_QUIT_RECORD = "settings.login.death-state-quit-record-location"
            @JvmField val SETTINGS_LOGIN_IP_REGISTER_LIMIT = "settings.login.ip-register-count-limit"
            @JvmField val SETTINGS_LOGIN_IP_COUNT_LIMIT = "settings.login.ip-count-limit"
            @JvmField val SETTINGS_LOGIN_SAME_IP_ENABLED = "settings.login.same-ip-login.enabled"
            @JvmField val SETTINGS_LOGIN_SAME_IP_TIMEOUT = "settings.login.same-ip-login.timeout"
            @JvmField val TWO_FACTOR_ENABLED = "two-factor.enabled"
            @JvmField val TWO_FACTOR_ISSUER = "two-factor.issuer"
            @JvmField val TWO_FACTOR_DIGITS = "two-factor.digits"
            @JvmField val TWO_FACTOR_PERIOD = "two-factor.period"
            @JvmField val TWO_FACTOR_RECOVERY_CODE_COUNT = "two-factor.recovery-code-count"
            @JvmField val TWO_FACTOR_MAX_ATTEMPTS = "two-factor.max-attempts"
            @JvmField val TWO_FACTOR_ATTEMPT_WINDOW = "two-factor.attempt-window"
            @JvmField val TWO_FACTOR_PENDING_TIMEOUT = "two-factor.pending-timeout"
            @JvmField val BEDROCK_LOGIN_BYPASS = "bedrock.login-bypass"
            @JvmField val BEDROCK_FLOODGATE_PREFIX = "bedrock.floodgate-prefix-protect"
            @JvmField val SPAWN_LOCATION = "spawn.location"
            @JvmField val DATABASE_MYSQL = "database.mysql"
            @JvmField val DATABASE_HOST = "database.host"
            @JvmField val DATABASE_PORT = "database.port"
            @JvmField val DATABASE_NAME = "database.database"
            @JvmField val DATABASE_USER = "database.user"
            @JvmField val DATABASE_PASSWORD = "database.password"
            @JvmField val EMAIL_ENABLED = "email.enabled"
            @JvmField val EMAIL_ACCOUNT = "email.account"
            @JvmField val EMAIL_PASSWORD = "email.password"
            @JvmField val EMAIL_SMTP_HOST = "email.smtp-host"
            @JvmField val EMAIL_SMTP_PORT = "email.smtp-port"
            @JvmField val EMAIL_SSL_AUTH = "email.ssl-auth"
            @JvmField val EMAIL_FROM_NAME = "email.from-name"
            @JvmField val PROXY_ENABLED = "proxy.enabled"
            @JvmField val PROXY_HOST = "proxy.host"
            @JvmField val PROXY_PORT = "proxy.port"
            @JvmField val PROXY_AUTH_KEY = "proxy.auth-key"
            @JvmField val PROXY_LOGIN_SERVER_NAME = "proxy.login-server-name"
            @JvmField val LANGUAGE = "language"
        }
    }

    class Comment private constructor() {
        companion object {
            @JvmField val DATABASE_HOST = "数据库主机地址 (MySQL服务器IP)"
            @JvmField val DATABASE_PORT = "数据库端口 (MySQL默认3306)"
            @JvmField val DATABASE_NAME = "数据库名称"
            @JvmField val DATABASE_USER = "数据库用户名"
            @JvmField val DATABASE_PASSWORD = "数据库密码"
            @JvmField val PROXY_HOST = "代理服务器通信地址 (Bukkit用于监听BungeeCord/Velocity连接)"
            @JvmField val PROXY_PORT = "代理服务器通信端口 (Bukkit监听端口)"
            @JvmField val PROXY_AUTH_KEY = "代理通信认证密钥 (确保Bukkit与代理通信安全)"
            @JvmField val PROXY_LOGIN_SERVER_NAME = "登录服服务器名称 (在代理端配置的服务器名)"
        }
    }
}
