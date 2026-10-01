package cc.baka9.catseedlogin.common.config;

import cc.baka9.catseedlogin.common.api.CoreConfig;
import cc.baka9.catseedlogin.common.api.DatabaseConfig;
import cc.baka9.catseedlogin.common.api.EmailConfig;
import cc.baka9.catseedlogin.common.api.PlatformAdapter;
import cc.baka9.catseedlogin.common.api.ProxyConfig;
import cc.baka9.catseedlogin.common.i18n.I18n;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * 跨平台配置管理器：负责 config.yml 的创建、加载、合并默认值与持久化， 并以类型安全的方式暴露各配置项。
 *
 * <p>平台特有配置（如 Bukkit 的出生点 {@code Location}）由子类扩展。
 */
public class ConfigManager implements CoreConfig, DatabaseConfig, ProxyConfig, EmailConfig {

  private static final Logger LOGGER = Logger.getLogger(ConfigManager.class.getName());

  protected final PlatformAdapter platform;
  protected I18n i18n;
  protected YamlConfiguration mainConfig;

  public ConfigManager(PlatformAdapter platform) {
    this.platform = platform;
    initConfig(platform.getDataFolder(), "config.yml");
  }

  protected void initConfig(File dataFolder, String configFileName) {
    if (!dataFolder.exists()) {
      dataFolder.mkdirs();
    }
    createDefaultConfig(configFileName);
    mainConfig = getConfig(configFileName);
    i18n = new I18n(dataFolder, this::getResource);
    applyLanguage();
  }

  private void applyLanguage() {
    String language =
        getMainConfig().getString(ConfigConstants.Path.LANGUAGE, ConfigConstants.DEFAULT_LANGUAGE);
    i18n.setLocale(language.replace("_", "-"));
  }

  public PlatformAdapter getPlatform() {
    return platform;
  }

  public InputStream getResource(String name) {
    return platform.getResource(name);
  }

  public YamlConfiguration getConfig(String name) {
    String fileName = name.endsWith(".yml") ? name : name + ".yml";
    File file = new File(platform.getDataFolder(), fileName);
    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

    try (InputStream defaultStream = getResource(fileName)) {
      if (defaultStream != null) {
        YamlConfiguration defaultConfig = new YamlConfiguration(null);
        defaultConfig.loadFromResource(defaultStream);
        if (mergeDefaults(config, defaultConfig)) {
          config.save();
        }
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Failed to load default config: " + name, e);
    }

    return config;
  }

  public void createDefaultConfig(String name) {
    String fileName = name.endsWith(".yml") ? name : name + ".yml";
    File file = new File(platform.getDataFolder(), fileName);
    if (!file.exists()) {
      try (InputStream in = getResource(fileName)) {
        if (in != null) {
          java.nio.file.Files.copy(in, file.toPath());
        }
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Failed to create default config: " + name, e);
      }
    }
  }

  public void saveConfig(String name) {
    String fileName = name.endsWith(".yml") ? name : name + ".yml";
    if (mainConfig != null
        && mainConfig.getFile() != null
        && mainConfig.getFile().getName().equals(fileName)) {
      mainConfig.saveQuietly();
      return;
    }
    getConfig(name).saveQuietly();
  }

  /** 写入配置项并立即持久化。 */
  public void set(String path, Object value) {
    mainConfig.set(path, value);
    saveConfig("config.yml");
  }

  /** 读取布尔配置并取反后写回，返回取反后的值，用于开关类指令。 */
  public boolean toggle(String path, boolean defaultValue) {
    boolean next = !mainConfig.getBoolean(path, defaultValue);
    set(path, next);
    return next;
  }

  /** 覆盖登录前指令白名单（正则字符串列表）。 */
  public void setCommandWhiteList(List<String> patterns) {
    set(ConfigConstants.Path.SETTINGS_COMMAND_WHITELIST, new ArrayList<>(patterns));
  }

  public void reload() {
    createDefaultConfig("config.yml");
    mainConfig = getConfig("config.yml");
    applyLanguage();
    i18n.reload();
  }

  public I18n getI18n() {
    return i18n;
  }

  public YamlConfiguration getMainConfig() {
    return mainConfig;
  }

  // ---- core settings ----

  @Override
  public int getIpRegisterCountLimit() {
    return mainConfig.getInt(
        ConfigConstants.Path.SETTINGS_IP_REGISTER_LIMIT, ConfigConstants.DEFAULT_IP_REGISTER_LIMIT);
  }

  @Override
  public int getIpCountLimit() {
    return mainConfig.getInt(
        ConfigConstants.Path.SETTINGS_IP_COUNT_LIMIT, ConfigConstants.DEFAULT_IP_LOGIN_LIMIT);
  }

  @Override
  public boolean isLimitChineseID() {
    return mainConfig.getBoolean(ConfigConstants.Path.SETTINGS_LIMIT_CHINESE_ID, true);
  }

  @Override
  public boolean isBedrockLoginBypass() {
    return mainConfig.getBoolean(ConfigConstants.Path.BEDROCK_LOGIN_BYPASS, true);
  }

  @Override
  public boolean isLoginWithSameIP() {
    return mainConfig.getBoolean(ConfigConstants.Path.SAME_IP_ENABLED, false);
  }

  @Override
  public boolean isEmptyBackpack() {
    return mainConfig.getBoolean(ConfigConstants.Path.EMPTY_BACKPACK, true);
  }

  @Override
  public int getIPTimeout() {
    return mainConfig.getInt(
        ConfigConstants.Path.SAME_IP_TIMEOUT, ConfigConstants.DEFAULT_IP_TIMEOUT_MINUTES);
  }

  @Override
  public int getMaxLengthID() {
    return mainConfig.getInt(
        ConfigConstants.Path.SETTINGS_MAX_LENGTH_ID, ConfigConstants.DEFAULT_MAX_NAME_LENGTH);
  }

  @Override
  public int getMinLengthID() {
    return mainConfig.getInt(
        ConfigConstants.Path.SETTINGS_MIN_LENGTH_ID, ConfigConstants.DEFAULT_MIN_NAME_LENGTH);
  }

  @Override
  public boolean isBeforeLoginNoDamage() {
    return mainConfig.getBoolean(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_NO_DAMAGE, true);
  }

  @Override
  public long getReenterInterval() {
    return mainConfig.getLong(
        ConfigConstants.Path.SETTINGS_REENTER_INTERVAL,
        ConfigConstants.DEFAULT_REENTER_INTERVAL_TICKS);
  }

  @Override
  public boolean isAfterLoginBack() {
    return mainConfig.getBoolean(ConfigConstants.Path.SETTINGS_AFTER_LOGIN_BACK, true);
  }

  @Override
  public boolean isCanTpSpawnLocation() {
    return mainConfig.getBoolean(ConfigConstants.Path.SETTINGS_CAN_TP_SPAWN_LOCATION, true);
  }

  @Override
  public int getAutoKick() {
    return mainConfig.getInt(
        ConfigConstants.Path.SETTINGS_AUTO_KICK, ConfigConstants.DEFAULT_AUTO_KICK_SECONDS);
  }

  @Override
  public String getNamePattern() {
    return mainConfig.getString(
        ConfigConstants.Path.SETTINGS_NAME_PATTERN, ConfigConstants.DEFAULT_NAME_PATTERN);
  }

  @Override
  public boolean isDeathStateQuitRecordLocation() {
    return mainConfig.getBoolean(ConfigConstants.Path.SETTINGS_DEATH_STATE_QUIT_RECORD, true);
  }

  @Override
  public boolean isFloodgatePrefixProtect() {
    return mainConfig.getBoolean(ConfigConstants.Path.BEDROCK_FLOODGATE_PREFIX, true);
  }

  @Override
  public boolean isLoopbackLoginBypass() {
    return mainConfig.getBoolean(ConfigConstants.Path.SETTINGS_LOOPBACK_LOGIN_BYPASS, false);
  }

  @Override
  public boolean isBeforeLoginAllowChat() {
    return mainConfig.getBoolean(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_ALLOW_CHAT, false);
  }

  @Override
  public boolean isBlindingBeforeLogin() {
    return mainConfig.getBoolean(ConfigConstants.Path.SETTINGS_BLINDING_BEFORE_LOGIN, false);
  }

  @Override
  public List<Pattern> getCommandWhiteList() {
    return ConfigConstants.compilePatterns(
        mainConfig.getStringList(ConfigConstants.Path.SETTINGS_COMMAND_WHITELIST));
  }

  // ---- database ----

  @Override
  public boolean isMySQL() {
    return mainConfig.getBoolean(ConfigConstants.Path.DATABASE_MYSQL, false);
  }

  @Override
  public String getDatabaseHost() {
    return mainConfig.getString(
        ConfigConstants.Path.DATABASE_HOST, ConfigConstants.DEFAULT_PROXY_HOST);
  }

  @Override
  public int getDatabasePort() {
    return mainConfig.getInt(
        ConfigConstants.Path.DATABASE_PORT, ConfigConstants.DEFAULT_MYSQL_PORT);
  }

  @Override
  public String getDatabaseName() {
    return mainConfig.getString(
        ConfigConstants.Path.DATABASE_NAME, ConfigConstants.DEFAULT_DATABASE_NAME);
  }

  @Override
  public String getDatabaseUser() {
    return mainConfig.getString(ConfigConstants.Path.DATABASE_USER, "root");
  }

  @Override
  public String getDatabasePassword() {
    return mainConfig.getString(ConfigConstants.Path.DATABASE_PASSWORD, "password");
  }

  // ---- proxy ----

  @Override
  public boolean isProxyEnabled() {
    return mainConfig.getBoolean(ConfigConstants.Path.PROXY_ENABLED, false);
  }

  @Override
  public String getProxyHost() {
    return mainConfig.getString(
        ConfigConstants.Path.PROXY_HOST, ConfigConstants.DEFAULT_PROXY_HOST);
  }

  @Override
  public int getProxyPort() {
    return mainConfig.getInt(ConfigConstants.Path.PROXY_PORT, ConfigConstants.DEFAULT_PROXY_PORT);
  }

  @Override
  public String getAuthKey() {
    return mainConfig.getString(ConfigConstants.Path.PROXY_AUTH_KEY, "");
  }

  @Override
  public String getLoginServerName() {
    return mainConfig.getString(ConfigConstants.Path.PROXY_LOGIN_SERVER_NAME, "lobby");
  }

  // ---- email ----

  @Override
  public boolean isEmailEnable() {
    return mainConfig.getBoolean(ConfigConstants.Path.EMAIL_ENABLED, false);
  }

  @Override
  public String getEmailAccount() {
    return mainConfig.getString(ConfigConstants.Path.EMAIL_ACCOUNT, "");
  }

  @Override
  public String getEmailPassword() {
    return mainConfig.getString(ConfigConstants.Path.EMAIL_PASSWORD, "");
  }

  @Override
  public String getEmailSmtpHost() {
    return mainConfig.getString(
        ConfigConstants.Path.EMAIL_SMTP_HOST, ConfigConstants.DEFAULT_SMTP_HOST);
  }

  @Override
  public String getEmailSmtpPort() {
    return mainConfig.getString(
        ConfigConstants.Path.EMAIL_SMTP_PORT, ConfigConstants.DEFAULT_SMTP_PORT);
  }

  @Override
  public boolean isSSLAuthVerify() {
    return mainConfig.getBoolean(ConfigConstants.Path.EMAIL_SSL_AUTH, true);
  }

  @Override
  public String getFromPersonal() {
    return mainConfig.getString(
        ConfigConstants.Path.EMAIL_FROM_NAME, ConfigConstants.DEFAULT_FROM_NAME);
  }

  // ---- defaults merging ----

  @SuppressWarnings("unchecked")
  private boolean mergeDefaults(YamlConfiguration config, YamlConfiguration defaults) {
    return mergeMap(config.getDataMap(), defaults.getDataMap());
  }

  @SuppressWarnings("unchecked")
  private boolean mergeMap(Map<String, Object> configMap, Map<String, Object> defaultMap) {
    boolean changed = false;
    for (Map.Entry<String, Object> entry : defaultMap.entrySet()) {
      String key = entry.getKey();
      Object defaultVal = entry.getValue();
      Object configVal = configMap.get(key);
      if (configVal == null) {
        configMap.put(key, defaultVal);
        changed = true;
      } else if (defaultVal instanceof Map && configVal instanceof Map) {
        if (mergeMap((Map<String, Object>) configVal, (Map<String, Object>) defaultVal)) {
          changed = true;
        }
      }
    }
    return changed;
  }
}
