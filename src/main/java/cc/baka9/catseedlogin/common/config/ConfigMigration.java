package cc.baka9.catseedlogin.common.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 旧版 config.yml 路径迁移。
 *
 * <p>配置文件重新归组后：settings 拆分为 account / before-login / login 三个子分组，
 * 依赖数据库记录的配置项（IP 数量限制、死亡退出位置记录、同IP免登录）下沉到 database 节点。
 *
 * <p>旧版配置文件里这些值仍在老路径上，如果直接读取新路径就会静默回退到默认值，
 * 等于用户升级后配置被重置。这里在加载（并补全默认值）之后把旧路径的值搬到新路径。
 */
public final class ConfigMigration {

  private static final Logger LOGGER = Logger.getLogger(ConfigMigration.class.getName());

  /** 旧路径 -> 新路径 */
  private static final Map<String, String> LEGACY_PATHS = new LinkedHashMap<>();

  /** 迁移后可能残留的空节点 */
  private static final String[] LEGACY_SECTIONS = {"same-ip-login", "settings"};

  static {
    // 依赖数据库记录的配置 -> database
    LEGACY_PATHS.put(
        "settings.ip-register-count-limit", ConfigConstants.Path.DATABASE_IP_REGISTER_LIMIT);
    LEGACY_PATHS.put("settings.ip-count-limit", ConfigConstants.Path.DATABASE_IP_COUNT_LIMIT);
    LEGACY_PATHS.put(
        "settings.death-state-quit-record-location",
        ConfigConstants.Path.DATABASE_DEATH_STATE_QUIT_RECORD);
    LEGACY_PATHS.put("same-ip-login.enabled", ConfigConstants.Path.DATABASE_SAME_IP_ENABLED);
    LEGACY_PATHS.put("same-ip-login.timeout", ConfigConstants.Path.DATABASE_SAME_IP_TIMEOUT);

    // settings -> settings.account
    LEGACY_PATHS.put(
        "settings.limit-chinese-id", ConfigConstants.Path.SETTINGS_ACCOUNT_LIMIT_CHINESE_ID);
    LEGACY_PATHS.put("settings.min-length-id", ConfigConstants.Path.SETTINGS_ACCOUNT_MIN_LENGTH_ID);
    LEGACY_PATHS.put("settings.max-length-id", ConfigConstants.Path.SETTINGS_ACCOUNT_MAX_LENGTH_ID);
    LEGACY_PATHS.put("settings.name-pattern", ConfigConstants.Path.SETTINGS_ACCOUNT_NAME_PATTERN);

    // settings -> settings.before-login
    LEGACY_PATHS.put(
        "settings.before-login-no-damage", ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_NO_DAMAGE);
    LEGACY_PATHS.put(
        "settings.can-tp-spawn-location", ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_CAN_TP_SPAWN);
    LEGACY_PATHS.put(
        "settings.before-login-allow-chat", ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_ALLOW_CHAT);
    LEGACY_PATHS.put(
        "settings.blinding-before-login", ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_BLINDING);
    LEGACY_PATHS.put("empty-backpack", ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_EMPTY_BACKPACK);
    LEGACY_PATHS.put("settings.auto-kick", ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_AUTO_KICK);
    LEGACY_PATHS.put(
        "settings.command-white-list",
        ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_COMMAND_WHITELIST);

    // settings -> settings.login
    LEGACY_PATHS.put(
        "settings.loopback-login-bypass", ConfigConstants.Path.SETTINGS_LOGIN_LOOPBACK_BYPASS);
    LEGACY_PATHS.put(
        "settings.reenter-interval", ConfigConstants.Path.SETTINGS_LOGIN_REENTER_INTERVAL);
    LEGACY_PATHS.put(
        "settings.after-login-back", ConfigConstants.Path.SETTINGS_LOGIN_AFTER_LOGIN_BACK);
  }

  private ConfigMigration() {}

  /**
   * 将配置中仍存在的旧路径迁移到新路径。
   *
   * <p>分两步执行：先把旧路径的值全部读出来缓存，再按对照表写回新路径。
   * 读与写分开可以避免边遍历边修改配置导致的结果依赖遍历顺序。
   *
   * @return 配置是否发生了改动（需要回写文件）
   */
  public static boolean migrate(YamlConfiguration config) {
    Map<String, Object> legacyValues = readLegacyValues(config);
    if (legacyValues.isEmpty()) {
      return removeEmptySections(config);
    }

    // 先清掉旧节点，再写入新路径
    for (String oldPath : legacyValues.keySet()) {
      config.set(oldPath, null);
    }
    int moved = 0;
    for (Map.Entry<String, String> entry : LEGACY_PATHS.entrySet()) {
      if (!legacyValues.containsKey(entry.getKey())) {
        continue;
      }
      config.set(entry.getValue(), legacyValues.get(entry.getKey()));
      moved++;
    }

    boolean changed = removeEmptySections(config) || moved > 0;
    LOGGER.log(Level.INFO, "已将 {0} 个旧配置路径迁移到新结构，旧路径已自动移除。", moved);
    return changed;
  }

  /** 按对照表把配置文件里现存的旧路径值缓存下来，后续迁移只依赖这份快照。 */
  private static Map<String, Object> readLegacyValues(YamlConfiguration config) {
    Map<String, Object> values = new LinkedHashMap<>();
    for (String oldPath : LEGACY_PATHS.keySet()) {
      if (!config.contains(oldPath)) {
        continue;
      }
      values.put(oldPath, config.get(oldPath));
    }
    return values;
  }

  /** 清理迁移后残留的空节点，避免留下无意义的 {@code same-ip-login: {}}。 */
  private static boolean removeEmptySections(YamlConfiguration config) {
    Map<String, Object> root = config.getDataMap();
    boolean changed = false;
    for (String section : LEGACY_SECTIONS) {
      Object value = root.get(section);
      if (value instanceof Map && ((Map<?, ?>) value).isEmpty()) {
        root.remove(section);
        changed = true;
      }
    }
    return changed;
  }
}
