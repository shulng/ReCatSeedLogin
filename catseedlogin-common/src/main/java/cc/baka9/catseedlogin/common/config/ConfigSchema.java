package cc.baka9.catseedlogin.common.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Single source of truth for all config keys, defaults, and validation.
 * Replaces ConfigConstants from v2.
 */
public final class ConfigSchema {

    public static final int CURRENT_VERSION = 1;

    // --- Config path constants ---
    public static final String KEY_CONFIG_VERSION = "config-version";
    public static final String KEY_LANGUAGE = "language";

    // Settings
    public static final String KEY_IP_REGISTER_LIMIT = "settings.ip-register-count-limit";
    public static final String KEY_IP_LOGIN_LIMIT = "settings.ip-count-limit";
    public static final String KEY_LIMIT_CHINESE_ID = "settings.limit-chinese-id";
    public static final String KEY_BEDROCK_LOGIN_BYPASS = "bedrock.login-bypass";
    public static final String KEY_BEDROCK_FLOODGATE_PREFIX_PROTECT = "bedrock.floodgate-prefix-protect";
    public static final String KEY_SAME_IP_LOGIN = "same-ip-login.enabled";
    public static final String KEY_SAME_IP_LOGIN_TIMEOUT = "same-ip-login.timeout";
    public static final String KEY_EMPTY_BACKPACK = "empty-backpack";
    public static final String KEY_IP_TIMEOUT = "settings.ip-timeout";
    public static final String KEY_MIN_LENGTH_ID = "settings.min-length-id";
    public static final String KEY_MAX_LENGTH_ID = "settings.max-length-id";
    public static final String KEY_BEFORE_LOGIN_NO_DAMAGE = "settings.before-login-no-damage";
    public static final String KEY_REENTER_INTERVAL = "settings.reenter-interval";
    public static final String KEY_AFTER_LOGIN_BACK = "settings.after-login-back";
    public static final String KEY_CAN_TP_SPAWN_LOCATION = "settings.can-tp-spawn-location";
    public static final String KEY_AUTO_KICK = "settings.auto-kick";
    public static final String KEY_NAME_PATTERN = "settings.name-pattern";
    public static final String KEY_DEATH_STATE_QUIT_RECORD = "settings.death-state-quit-record-location";
    public static final String KEY_COMMAND_WHITELIST = "settings.command-white-list";
    public static final String KEY_SPAWN_LOCATION = "spawn.location";

    // Database
    public static final String KEY_DB_TYPE = "database.type";
    public static final String KEY_DB_HOST = "database.host";
    public static final String KEY_DB_PORT = "database.port";
    public static final String KEY_DB_NAME = "database.database";
    public static final String KEY_DB_USER = "database.user";
    public static final String KEY_DB_PASSWORD = "database.password";

    // Email
    public static final String KEY_EMAIL_ENABLE = "email.enabled";
    public static final String KEY_EMAIL_ACCOUNT = "email.account";
    public static final String KEY_EMAIL_PASSWORD = "email.password";
    public static final String KEY_EMAIL_SMTP_HOST = "email.smtp-host";
    public static final String KEY_EMAIL_SMTP_PORT = "email.smtp-port";
    public static final String KEY_EMAIL_SSL_AUTH = "email.ssl-auth";
    public static final String KEY_EMAIL_FROM_NAME = "email.from-name";

    // Proxy
    public static final String KEY_PROXY_ENABLE = "proxy.enabled";
    public static final String KEY_PROXY_HOST = "proxy.host";
    public static final String KEY_PROXY_PORT = "proxy.port";
    public static final String KEY_PROXY_AUTH_KEY = "proxy.auth-key";
    public static final String KEY_PROXY_LOGIN_SERVER = "proxy.login-server-name";

    // Password
    public static final String KEY_PASSWORD_ALGORITHM = "password.hash-algorithm";

    // --- Default values ---
    public static final String DEFAULT_LANGUAGE = "zh_CN";
    public static final int DEFAULT_IP_REGISTER_LIMIT = 2;
    public static final int DEFAULT_IP_LOGIN_LIMIT = 2;
    public static final boolean DEFAULT_LIMIT_CHINESE_ID = true;
    public static final boolean DEFAULT_BEDROCK_LOGIN_BYPASS = true;
    public static final boolean DEFAULT_BEDROCK_FLOODGATE_PREFIX_PROTECT = true;
    public static final boolean DEFAULT_SAME_IP_LOGIN = false;
    public static final int DEFAULT_SAME_IP_LOGIN_TIMEOUT = 5;
    public static final boolean DEFAULT_EMPTY_BACKPACK = true;
    public static final int DEFAULT_IP_TIMEOUT = 5;
    public static final int DEFAULT_MIN_LENGTH_ID = 2;
    public static final int DEFAULT_MAX_LENGTH_ID = 15;
    public static final boolean DEFAULT_BEFORE_LOGIN_NO_DAMAGE = true;
    public static final long DEFAULT_REENTER_INTERVAL = 60L;
    public static final boolean DEFAULT_AFTER_LOGIN_BACK = true;
    public static final boolean DEFAULT_CAN_TP_SPAWN = true;
    public static final int DEFAULT_AUTO_KICK = 120;
    public static final String DEFAULT_NAME_PATTERN = "^\\w+$";
    public static final boolean DEFAULT_DEATH_STATE_QUIT_RECORD = true;
    public static final String DEFAULT_SPAWN_LOCATION = "";
    public static final boolean DEFAULT_DB_MYSQL = false;
    public static final String DEFAULT_DB_HOST = "localhost";
    public static final int DEFAULT_DB_PORT = 3306;
    public static final String DEFAULT_DB_NAME = "catseedlogin";
    public static final String DEFAULT_DB_USER = "root";
    public static final String DEFAULT_DB_PASSWORD = "";
    public static final boolean DEFAULT_EMAIL_ENABLE = false;
    public static final String DEFAULT_EMAIL_ACCOUNT = "";
    public static final String DEFAULT_EMAIL_PASSWORD = "";
    public static final String DEFAULT_EMAIL_SMTP_HOST = "smtp.example.com";
    public static final int DEFAULT_EMAIL_SMTP_PORT = 465;
    public static final boolean DEFAULT_EMAIL_SSL_AUTH = true;
    public static final String DEFAULT_EMAIL_FROM_NAME = "Server";
    public static final boolean DEFAULT_PROXY_ENABLE = false;
    public static final String DEFAULT_PROXY_HOST = "127.0.0.1";
    public static final int DEFAULT_PROXY_PORT = 2333;
    public static final String DEFAULT_PROXY_AUTH_KEY = "";
    public static final String DEFAULT_PROXY_LOGIN_SERVER = "lobby";
    public static final String DEFAULT_PASSWORD_ALGORITHM = "argon2id";

    public static final List<String> DEFAULT_COMMAND_WHITELIST = Collections.unmodifiableList(Arrays.asList(
            "^/(login|l|register|reg)$",
            "^/(resetpassword|repw)\\b.*",
            "^/(changepassword|changepw)\\b.*",
            "^/(bindemail|bdmail)\\b.*",
            "^/(cslv|cslb)\\b.*",
            "^/cui(\\s|$)"
    ));

    private ConfigSchema() {}

    /**
     * Compile command whitelist patterns from config strings.
     */
    public static List<Pattern> compilePatterns(List<String> patterns) {
        if (patterns == null) return Collections.emptyList();
        return patterns.stream()
                .filter(s -> s != null && !s.isEmpty())
                .map(s -> {
                    try {
                        return Pattern.compile(s);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(p -> p != null)
                .collect(java.util.stream.Collectors.toList());
    }
}
