package cc.baka9.catseedlogin.common.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ConfigConstants {

  public static final String DEFAULT_LANGUAGE = "zh_CN";
  public static final String DEFAULT_PROXY_HOST = "127.0.0.1";
  public static final int DEFAULT_PROXY_PORT = 2333;
  public static final int DEFAULT_MYSQL_PORT = 3306;
  public static final String DEFAULT_DATABASE_NAME = "catseedlogin";
  public static final String DEFAULT_SPAWN_LOCATION = "";
  public static final String DEFAULT_NAME_PATTERN = "^\\w+$";
  public static final int DEFAULT_IP_REGISTER_LIMIT = 2;
  public static final int DEFAULT_IP_LOGIN_LIMIT = 2;
  public static final int DEFAULT_MIN_NAME_LENGTH = 2;
  public static final int DEFAULT_MAX_NAME_LENGTH = 15;
  public static final int DEFAULT_AUTO_KICK_SECONDS = 120;
  public static final int DEFAULT_REENTER_INTERVAL_TICKS = 60;
  public static final int DEFAULT_IP_TIMEOUT_MINUTES = 5;
  public static final String DEFAULT_SMTP_HOST = "smtp.example.com";
  public static final String DEFAULT_SMTP_PORT = "465";
  public static final String DEFAULT_FROM_NAME = "Server";

  public static final List<String> DEFAULT_COMMAND_WHITELIST = new ArrayList<>();

  static {
    DEFAULT_COMMAND_WHITELIST.add("/(?i)l(ogin)?(\\z| .*)");
    DEFAULT_COMMAND_WHITELIST.add("/(?i)reg(ister)?(\\z| .*)");
    DEFAULT_COMMAND_WHITELIST.add("/(?i)resetpassword?(\\z| .*)");
    DEFAULT_COMMAND_WHITELIST.add("/(?i)repw?(\\z| .*)");
  }

  public static Pattern compilePatternOrDefault(String pattern, String defaultPattern) {
    if (pattern == null || pattern.isEmpty()) {
      return Pattern.compile(defaultPattern);
    }
    try {
      return Pattern.compile(pattern);
    } catch (PatternSyntaxException e) {
      return Pattern.compile(defaultPattern);
    }
  }

  public static List<Pattern> compilePatterns(List<String> patterns) {
    List<Pattern> result = new ArrayList<>();
    if (patterns == null || patterns.isEmpty()) {
      return result;
    }
    for (String pattern : patterns) {
      try {
        result.add(Pattern.compile(pattern));
      } catch (PatternSyntaxException e) {
        // Skip invalid patterns
      }
    }
    return result;
  }

  public static class Path {
    // ---- settings.account 账号规则 ----
    public static final String SETTINGS_ACCOUNT_LIMIT_CHINESE_ID =
        "settings.account.limit-chinese-id";
    public static final String SETTINGS_ACCOUNT_MIN_LENGTH_ID = "settings.account.min-length-id";
    public static final String SETTINGS_ACCOUNT_MAX_LENGTH_ID = "settings.account.max-length-id";
    public static final String SETTINGS_ACCOUNT_NAME_PATTERN = "settings.account.name-pattern";

    // ---- settings.before-login 登录前限制 ----
    public static final String SETTINGS_BEFORE_LOGIN_NO_DAMAGE = "settings.before-login.no-damage";
    public static final String SETTINGS_BEFORE_LOGIN_CAN_TP_SPAWN =
        "settings.before-login.can-tp-spawn-location";
    public static final String SETTINGS_BEFORE_LOGIN_ALLOW_CHAT =
        "settings.before-login.allow-chat";
    public static final String SETTINGS_BEFORE_LOGIN_BLINDING = "settings.before-login.blinding";
    public static final String SETTINGS_BEFORE_LOGIN_EMPTY_BACKPACK =
        "settings.before-login.empty-backpack";
    public static final String SETTINGS_BEFORE_LOGIN_AUTO_KICK = "settings.before-login.auto-kick";
    public static final String SETTINGS_BEFORE_LOGIN_COMMAND_WHITELIST =
        "settings.before-login.command-white-list";

    // ---- settings.login 登录流程 ----
    public static final String SETTINGS_LOGIN_LOOPBACK_BYPASS = "settings.login.loopback-bypass";
    public static final String SETTINGS_LOGIN_REENTER_INTERVAL = "settings.login.reenter-interval";
    public static final String SETTINGS_LOGIN_AFTER_LOGIN_BACK = "settings.login.after-login-back";

    public static final String BEDROCK_LOGIN_BYPASS = "bedrock.login-bypass";
    public static final String BEDROCK_FLOODGATE_PREFIX = "bedrock.floodgate-prefix-protect";

    public static final String SPAWN_LOCATION = "spawn.location";

    // ---- database 连接信息 ----
    public static final String DATABASE_MYSQL = "database.mysql";
    public static final String DATABASE_HOST = "database.host";
    public static final String DATABASE_PORT = "database.port";
    public static final String DATABASE_NAME = "database.database";
    public static final String DATABASE_USER = "database.user";
    public static final String DATABASE_PASSWORD = "database.password";

    // ---- database 数据存储相关 ----
    public static final String DATABASE_IP_REGISTER_LIMIT = "database.ip-register-count-limit";
    public static final String DATABASE_IP_COUNT_LIMIT = "database.ip-count-limit";
    public static final String DATABASE_DEATH_STATE_QUIT_RECORD =
        "database.death-state-quit-record-location";
    public static final String DATABASE_SAME_IP_ENABLED = "database.same-ip-login.enabled";
    public static final String DATABASE_SAME_IP_TIMEOUT = "database.same-ip-login.timeout";

    public static final String EMAIL_ENABLED = "email.enabled";
    public static final String EMAIL_ACCOUNT = "email.account";
    public static final String EMAIL_PASSWORD = "email.password";
    public static final String EMAIL_SMTP_HOST = "email.smtp-host";
    public static final String EMAIL_SMTP_PORT = "email.smtp-port";
    public static final String EMAIL_SSL_AUTH = "email.ssl-auth";
    public static final String EMAIL_FROM_NAME = "email.from-name";

    public static final String PROXY_ENABLED = "proxy.enabled";
    public static final String PROXY_HOST = "proxy.host";
    public static final String PROXY_PORT = "proxy.port";
    public static final String PROXY_AUTH_KEY = "proxy.auth-key";
    public static final String PROXY_LOGIN_SERVER_NAME = "proxy.login-server-name";

    public static final String LANGUAGE = "language";
  }

  public static class Comment {
    public static final String DATABASE_HOST = "数据库主机地址 (MySQL服务器IP)";
    public static final String DATABASE_PORT = "数据库端口 (MySQL默认3306)";
    public static final String DATABASE_NAME = "数据库名称";
    public static final String DATABASE_USER = "数据库用户名";
    public static final String DATABASE_PASSWORD = "数据库密码";

    public static final String PROXY_HOST = "代理服务器通信地址 (Bukkit用于监听BungeeCord/Velocity连接)";
    public static final String PROXY_PORT = "代理服务器通信端口 (Bukkit监听端口)";
    public static final String PROXY_AUTH_KEY = "代理通信认证密钥 (确保Bukkit与代理通信安全)";
    public static final String PROXY_LOGIN_SERVER_NAME = "登录服服务器名称 (在代理端配置的服务器名)";
  }
}
