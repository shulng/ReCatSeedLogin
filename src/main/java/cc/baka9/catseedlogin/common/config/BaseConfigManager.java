package cc.baka9.catseedlogin.common.config;

import cc.baka9.catseedlogin.common.api.CoreConfig;
import cc.baka9.catseedlogin.common.api.DatabaseConfig;
import cc.baka9.catseedlogin.common.api.BungeeCordConfig;
import cc.baka9.catseedlogin.common.api.EmailConfig;
import cc.baka9.catseedlogin.common.i18n.I18n;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public abstract class BaseConfigManager implements CoreConfig, DatabaseConfig, BungeeCordConfig, EmailConfig {

    private static final Logger LOGGER = Logger.getLogger(BaseConfigManager.class.getName());
    protected File dataFolder;
    protected I18n i18n;
    protected YamlConfiguration mainConfig;

    protected void initConfig(File dataFolder, String configFileName) {
        this.dataFolder = dataFolder;
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        createDefaultConfig(configFileName);
        mainConfig = getConfig(configFileName);
        i18n = new I18n(dataFolder, this::getResource);
        String language = getMainConfig().getString(ConfigConstants.Path.LANGUAGE, ConfigConstants.DEFAULT_LANGUAGE);
        i18n.setLocale(language.replace("_", "-"));
    }

    public abstract InputStream getResource(String name);

    public YamlConfiguration getConfig(String name) {
        String fileName = name.endsWith(".yml") ? name : name + ".yml";
        File file = new File(dataFolder, fileName);
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        try (InputStream defaultStream = getResource(fileName)) {
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = new YamlConfiguration(null);
                defaultConfig.loadFromResource(defaultStream);
                if (mergeDefaults(config, defaultConfig)) {
                    try {
                        config.save();
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Failed to save merged config: " + name, e);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load default config: " + name, e);
        }

        return config;
    }

    public void createDefaultConfig(String name) {
        String fileName = name.endsWith(".yml") ? name : name + ".yml";
        File file = new File(dataFolder, fileName);
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
        if (mainConfig != null && mainConfig.getFile() != null && mainConfig.getFile().getName().equals(fileName)) {
            try {
                mainConfig.save();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to save config: " + name, e);
            }
        } else {
            YamlConfiguration config = getConfig(name);
            if (config != null) {
                try {
                    config.save();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to save config: " + name, e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean mergeDefaults(YamlConfiguration config, YamlConfiguration defaults) {
        return mergeMap(config.getDataMap(), defaults.getDataMap());
    }

    @SuppressWarnings("unchecked")
    private boolean mergeMap(java.util.Map<String, Object> configMap, java.util.Map<String, Object> defaultMap) {
        boolean changed = false;
        for (java.util.Map.Entry<String, Object> entry : defaultMap.entrySet()) {
            String key = entry.getKey();
            Object defaultVal = entry.getValue();
            Object configVal = configMap.get(key);
            if (configVal == null) {
                configMap.put(key, defaultVal);
                changed = true;
            } else if (defaultVal instanceof java.util.Map && configVal instanceof java.util.Map) {
                if (mergeMap((java.util.Map<String, Object>) configVal, (java.util.Map<String, Object>) defaultVal)) {
                    changed = true;
                }
            }
        }
        return changed;
    }

    public void reload() {
        createDefaultConfig("config.yml");
        mainConfig = getConfig("config.yml");
        String language = mainConfig.getString(ConfigConstants.Path.LANGUAGE, ConfigConstants.DEFAULT_LANGUAGE);
        i18n.setLocale(language.replace("_", "-"));
        i18n.reload();
    }

    public I18n getI18n() {
        return i18n;
    }

    public YamlConfiguration getMainConfig() {
        return mainConfig;
    }

    @Override
    public int getIpRegisterCountLimit() {
        return mainConfig.getInt(ConfigConstants.Path.SETTINGS_IP_REGISTER_LIMIT, ConfigConstants.DEFAULT_IP_REGISTER_LIMIT);
    }

    @Override
    public int getIpCountLimit() {
        return mainConfig.getInt(ConfigConstants.Path.SETTINGS_IP_COUNT_LIMIT, ConfigConstants.DEFAULT_IP_LOGIN_LIMIT);
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
        return mainConfig.getInt(ConfigConstants.Path.SAME_IP_TIMEOUT, ConfigConstants.DEFAULT_IP_TIMEOUT_MINUTES);
    }

    @Override
    public int getMaxLengthID() {
        return mainConfig.getInt(ConfigConstants.Path.SETTINGS_MAX_LENGTH_ID, ConfigConstants.DEFAULT_MAX_NAME_LENGTH);
    }

    @Override
    public int getMinLengthID() {
        return mainConfig.getInt(ConfigConstants.Path.SETTINGS_MIN_LENGTH_ID, ConfigConstants.DEFAULT_MIN_NAME_LENGTH);
    }

    @Override
    public boolean isBeforeLoginNoDamage() {
        return mainConfig.getBoolean(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_NO_DAMAGE, true);
    }

    @Override
    public long getReenterInterval() {
        return mainConfig.getLong(ConfigConstants.Path.SETTINGS_REENTER_INTERVAL, ConfigConstants.DEFAULT_REENTER_INTERVAL_TICKS);
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
        return mainConfig.getInt(ConfigConstants.Path.SETTINGS_AUTO_KICK, ConfigConstants.DEFAULT_AUTO_KICK_SECONDS);
    }

    @Override
    public String getNamePattern() {
        return mainConfig.getString(ConfigConstants.Path.SETTINGS_NAME_PATTERN, ConfigConstants.DEFAULT_NAME_PATTERN);
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
    public List<Pattern> getCommandWhiteList() {
        List<String> patterns = mainConfig.getStringList(ConfigConstants.Path.SETTINGS_COMMAND_WHITELIST);
        return ConfigConstants.compilePatterns(patterns);
    }

    @Override
    public boolean isMySQL() {
        return mainConfig.getBoolean(ConfigConstants.Path.DATABASE_MYSQL, false);
    }

    @Override
    public String getDatabaseHost() {
        return mainConfig.getString(ConfigConstants.Path.DATABASE_HOST, ConfigConstants.DEFAULT_PROXY_HOST);
    }

    @Override
    public int getDatabasePort() {
        return mainConfig.getInt(ConfigConstants.Path.DATABASE_PORT, ConfigConstants.DEFAULT_MYSQL_PORT);
    }

    @Override
    public String getDatabaseName() {
        return mainConfig.getString(ConfigConstants.Path.DATABASE_NAME, ConfigConstants.DEFAULT_DATABASE_NAME);
    }

    @Override
    public String getDatabaseUser() {
        return mainConfig.getString(ConfigConstants.Path.DATABASE_USER, "root");
    }

    @Override
    public String getDatabasePassword() {
        return mainConfig.getString(ConfigConstants.Path.DATABASE_PASSWORD, "password");
    }

    @Override
    public boolean isEnable() {
        return mainConfig.getBoolean(ConfigConstants.Path.PROXY_ENABLED, false);
    }

    @Override
    public String getProxyHost() {
        return mainConfig.getString(ConfigConstants.Path.PROXY_HOST, ConfigConstants.DEFAULT_PROXY_HOST);
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
        return mainConfig.getString(ConfigConstants.Path.EMAIL_SMTP_HOST, ConfigConstants.DEFAULT_SMTP_HOST);
    }

    @Override
    public String getEmailSmtpPort() {
        return mainConfig.getString(ConfigConstants.Path.EMAIL_SMTP_PORT, ConfigConstants.DEFAULT_SMTP_PORT);
    }

    @Override
    public boolean isSSLAuthVerify() {
        return mainConfig.getBoolean(ConfigConstants.Path.EMAIL_SSL_AUTH, true);
    }

    @Override
    public String getFromPersonal() {
        return mainConfig.getString(ConfigConstants.Path.EMAIL_FROM_NAME, ConfigConstants.DEFAULT_FROM_NAME);
    }

    public void set(String path, Object value) {
        mainConfig.set(path, value);
        saveConfig("config.yml");
    }

    @Override
    public CoreConfig.SpawnLocation getSpawnLocation() {
        String locStr = mainConfig.getString(ConfigConstants.Path.SPAWN_LOCATION, ConfigConstants.DEFAULT_SPAWN_LOCATION);
        return parseSpawnLocation(locStr);
    }

    private CoreConfig.SpawnLocation parseSpawnLocation(String str) {
        ConfigHelper.LocationData data = ConfigHelper.parseLocationString(str, 
            ConfigHelper.LocationData.builder()
                .world("world")
                .x(0).y(64).z(0).yaw(0).pitch(0)
                .build());
        return new CoreConfig.SpawnLocation() {
            @Override
            public String getWorld() {
                return data.world;
            }

            @Override
            public double getX() {
                return data.x;
            }

            @Override
            public double getY() {
                return data.y;
            }

            @Override
            public double getZ() {
                return data.z;
            }

            @Override
            public float getYaw() {
                return data.yaw;
            }

            @Override
            public float getPitch() {
                return data.pitch;
            }
        };
    }
}
