package cc.baka9.catseedlogin.common.config

import cc.baka9.catseedlogin.common.api.BungeeCordConfig
import cc.baka9.catseedlogin.common.api.CoreConfig
import cc.baka9.catseedlogin.common.api.DatabaseConfig
import cc.baka9.catseedlogin.common.api.EmailConfig
import cc.baka9.catseedlogin.common.i18n.I18n
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.Pattern

abstract class BaseConfigManager : CoreConfig, DatabaseConfig, BungeeCordConfig, EmailConfig {
    companion object { private val LOGGER = Logger.getLogger(BaseConfigManager::class.java.name) }
    private lateinit var configDataFolder: File
    private lateinit var configI18n: I18n
    @get:JvmName("getMainConfigValue")
    lateinit var mainConfig: YamlConfiguration

    protected fun initConfig(dataFolder: File, configFileName: String) {
        this.configDataFolder = dataFolder
        if (!dataFolder.exists()) dataFolder.mkdirs()
        createDefaultConfig(configFileName)
        mainConfig = getConfig(configFileName)
        configI18n = I18n(dataFolder, this::getResource)
        val language = mainConfig.getString(ConfigConstants.Path.LANGUAGE, ConfigConstants.DEFAULT_LANGUAGE)
        configI18n.setLocale(language.replace("_", "-"))
    }

    abstract fun getResource(name: String): InputStream?

    fun getConfig(name: String): YamlConfiguration {
        val fileName = if (name.endsWith(".yml")) name else "$name.yml"
        val file = File(configDataFolder, fileName)
        val config = YamlConfiguration.loadConfiguration(file)
        try {
            getResource(fileName)?.use { stream ->
                val defaults = YamlConfiguration(null)
                defaults.loadFromResource(stream)
                var changed = mergeDefaults(config, defaults)
                if (fileName == "config.yml" && ConfigMigration.migrate(config)) changed = true
                if (changed) try { config.save() } catch (e: Exception) { LOGGER.log(Level.WARNING, "Failed to save merged config: $name", e) }
            }
        } catch (e: Exception) { LOGGER.log(Level.WARNING, "Failed to load default config: $name", e) }
        return config
    }

    fun createDefaultConfig(name: String) {
        val fileName = if (name.endsWith(".yml")) name else "$name.yml"
        val file = File(configDataFolder, fileName)
        if (!file.exists()) try { getResource(fileName)?.use { Files.copy(it, file.toPath()) } } catch (e: Exception) { LOGGER.log(Level.WARNING, "Failed to create default config: $name", e) }
    }

    fun saveConfig(name: String) {
        val fileName = if (name.endsWith(".yml")) name else "$name.yml"
        if (::mainConfig.isInitialized && mainConfig.getFile()?.name == fileName) {
            try { mainConfig.save() } catch (e: Exception) { LOGGER.log(Level.WARNING, "Failed to save config: $name", e) }
        } else try { getConfig(name).save() } catch (e: Exception) { LOGGER.log(Level.WARNING, "Failed to save config: $name", e) }
    }

    private fun mergeDefaults(config: YamlConfiguration, defaults: YamlConfiguration): Boolean = mergeMap(config.getDataMap(), defaults.getDataMap())
    private fun mergeMap(configMap: MutableMap<String, Any?>, defaultMap: MutableMap<String, Any?>): Boolean {
        var changed = false
        defaultMap.forEach { (key, defaultVal) ->
            val configVal = configMap[key]
            if (configVal == null) { configMap[key] = defaultVal; changed = true }
            else if (defaultVal is Map<*, *> && configVal is MutableMap<*, *>) {
                @Suppress("UNCHECKED_CAST")
                if (mergeMap(configVal as MutableMap<String, Any?>, defaultVal as MutableMap<String, Any?>)) changed = true
            }
        }
        return changed
    }

    fun reload() {
        createDefaultConfig("config.yml")
        mainConfig = getConfig("config.yml")
        val language = mainConfig.getString(ConfigConstants.Path.LANGUAGE, ConfigConstants.DEFAULT_LANGUAGE)
        configI18n.setLocale(language.replace("_", "-")); configI18n.reload()
    }

    fun getDataFolder(): File = configDataFolder
    fun getI18n(): I18n = configI18n
    fun getMainConfig(): YamlConfiguration = mainConfig

    override fun getIpRegisterCountLimit() = mainConfig.getInt(ConfigConstants.Path.SETTINGS_LOGIN_IP_REGISTER_LIMIT, ConfigConstants.DEFAULT_IP_REGISTER_LIMIT)
    override fun getIpCountLimit() = mainConfig.getInt(ConfigConstants.Path.SETTINGS_LOGIN_IP_COUNT_LIMIT, ConfigConstants.DEFAULT_IP_LOGIN_LIMIT)
    override fun isLimitChineseID() = mainConfig.getBoolean(ConfigConstants.Path.SETTINGS_ACCOUNT_LIMIT_CHINESE_ID, true)
    override fun isBedrockLoginBypass() = mainConfig.getBoolean(ConfigConstants.Path.BEDROCK_LOGIN_BYPASS, true)
    override fun isLoginWithSameIP() = mainConfig.getBoolean(ConfigConstants.Path.SETTINGS_LOGIN_SAME_IP_ENABLED, false)
    override fun isEmptyBackpack() = mainConfig.getBoolean(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_EMPTY_BACKPACK, true)
    override fun getIPTimeout() = mainConfig.getInt(ConfigConstants.Path.SETTINGS_LOGIN_SAME_IP_TIMEOUT, ConfigConstants.DEFAULT_IP_TIMEOUT_MINUTES)
    override fun getMaxLengthID() = mainConfig.getInt(ConfigConstants.Path.SETTINGS_ACCOUNT_MAX_LENGTH_ID, ConfigConstants.DEFAULT_MAX_NAME_LENGTH)
    override fun getMinLengthID() = mainConfig.getInt(ConfigConstants.Path.SETTINGS_ACCOUNT_MIN_LENGTH_ID, ConfigConstants.DEFAULT_MIN_NAME_LENGTH)
    override fun isBeforeLoginNoDamage() = mainConfig.getBoolean(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_NO_DAMAGE, true)
    override fun getReenterInterval() = mainConfig.getLong(ConfigConstants.Path.SETTINGS_LOGIN_REENTER_INTERVAL, ConfigConstants.DEFAULT_REENTER_INTERVAL_TICKS.toLong())
    override fun isAfterLoginBack() = mainConfig.getBoolean(ConfigConstants.Path.SETTINGS_LOGIN_AFTER_LOGIN_BACK, true)
    override fun isCanTpSpawnLocation() = mainConfig.getBoolean(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_CAN_TP_SPAWN, true)
    override fun getAutoKick() = mainConfig.getInt(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_AUTO_KICK, ConfigConstants.DEFAULT_AUTO_KICK_SECONDS)
    override fun getNamePattern() = mainConfig.getString(ConfigConstants.Path.SETTINGS_ACCOUNT_NAME_PATTERN, ConfigConstants.DEFAULT_NAME_PATTERN)
    override fun isDeathStateQuitRecordLocation() = mainConfig.getBoolean(ConfigConstants.Path.SETTINGS_LOGIN_DEATH_STATE_QUIT_RECORD, true)
    override fun isFloodgatePrefixProtect() = mainConfig.getBoolean(ConfigConstants.Path.BEDROCK_FLOODGATE_PREFIX, true)
    override fun isLoopbackLoginBypass() = mainConfig.getBoolean(ConfigConstants.Path.SETTINGS_LOGIN_LOOPBACK_BYPASS, false)
    override fun isBeforeLoginAllowChat() = mainConfig.getBoolean(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_ALLOW_CHAT, false)
    override fun isBlindingBeforeLogin() = mainConfig.getBoolean(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_BLINDING, false)
    override fun isTwoFactorEnabled() = mainConfig.getBoolean(ConfigConstants.Path.TWO_FACTOR_ENABLED, ConfigConstants.DEFAULT_TWO_FACTOR_ENABLED)
    override fun getTwoFactorIssuer() = mainConfig.getString(ConfigConstants.Path.TWO_FACTOR_ISSUER, ConfigConstants.DEFAULT_TWO_FACTOR_ISSUER)
    override fun getTwoFactorDigits() = mainConfig.getInt(ConfigConstants.Path.TWO_FACTOR_DIGITS, ConfigConstants.DEFAULT_TWO_FACTOR_DIGITS)
    override fun getTwoFactorPeriod() = mainConfig.getInt(ConfigConstants.Path.TWO_FACTOR_PERIOD, ConfigConstants.DEFAULT_TWO_FACTOR_PERIOD)
    override fun getTwoFactorRecoveryCodeCount() = mainConfig.getInt(ConfigConstants.Path.TWO_FACTOR_RECOVERY_CODE_COUNT, ConfigConstants.DEFAULT_TWO_FACTOR_RECOVERY_CODE_COUNT)
    override fun getTwoFactorMaxAttempts() = mainConfig.getInt(ConfigConstants.Path.TWO_FACTOR_MAX_ATTEMPTS, ConfigConstants.DEFAULT_TWO_FACTOR_MAX_ATTEMPTS)
    override fun getTwoFactorAttemptWindow() = mainConfig.getInt(ConfigConstants.Path.TWO_FACTOR_ATTEMPT_WINDOW, ConfigConstants.DEFAULT_TWO_FACTOR_ATTEMPT_WINDOW)
    override fun getTwoFactorPendingTimeout() = mainConfig.getInt(ConfigConstants.Path.TWO_FACTOR_PENDING_TIMEOUT, ConfigConstants.DEFAULT_TWO_FACTOR_PENDING_TIMEOUT)
    override fun getCommandWhiteList(): List<Pattern> = ConfigConstants.compilePatterns(mainConfig.getStringList(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_COMMAND_WHITELIST))
    override fun isMySQL() = mainConfig.getBoolean(ConfigConstants.Path.DATABASE_MYSQL, false)
    override fun getDatabaseHost() = mainConfig.getString(ConfigConstants.Path.DATABASE_HOST, ConfigConstants.DEFAULT_PROXY_HOST)
    override fun getDatabasePort() = mainConfig.getInt(ConfigConstants.Path.DATABASE_PORT, ConfigConstants.DEFAULT_MYSQL_PORT)
    override fun getDatabaseName() = mainConfig.getString(ConfigConstants.Path.DATABASE_NAME, ConfigConstants.DEFAULT_DATABASE_NAME)
    override fun getDatabaseUser() = mainConfig.getString(ConfigConstants.Path.DATABASE_USER, "root")
    override fun getDatabasePassword() = mainConfig.getString(ConfigConstants.Path.DATABASE_PASSWORD, "password")
    override fun isEnable() = mainConfig.getBoolean(ConfigConstants.Path.PROXY_ENABLED, false)
    override fun getProxyHost() = mainConfig.getString(ConfigConstants.Path.PROXY_HOST, ConfigConstants.DEFAULT_PROXY_HOST)
    override fun getProxyPort() = mainConfig.getInt(ConfigConstants.Path.PROXY_PORT, ConfigConstants.DEFAULT_PROXY_PORT)
    override fun getAuthKey() = mainConfig.getString(ConfigConstants.Path.PROXY_AUTH_KEY, "")
    override fun getLoginServerName() = mainConfig.getString(ConfigConstants.Path.PROXY_LOGIN_SERVER_NAME, "lobby")
    override fun isEmailEnable() = mainConfig.getBoolean(ConfigConstants.Path.EMAIL_ENABLED, false)
    override fun getEmailAccount() = mainConfig.getString(ConfigConstants.Path.EMAIL_ACCOUNT, "")
    override fun getEmailPassword() = mainConfig.getString(ConfigConstants.Path.EMAIL_PASSWORD, "")
    override fun getEmailSmtpHost() = mainConfig.getString(ConfigConstants.Path.EMAIL_SMTP_HOST, ConfigConstants.DEFAULT_SMTP_HOST)
    override fun getEmailSmtpPort() = mainConfig.getString(ConfigConstants.Path.EMAIL_SMTP_PORT, ConfigConstants.DEFAULT_SMTP_PORT)
    override fun isSSLAuthVerify() = mainConfig.getBoolean(ConfigConstants.Path.EMAIL_SSL_AUTH, true)
    override fun getFromPersonal() = mainConfig.getString(ConfigConstants.Path.EMAIL_FROM_NAME, ConfigConstants.DEFAULT_FROM_NAME)

    fun set(path: String, value: Any?) { mainConfig.set(path, value); saveConfig("config.yml") }

    override fun getSpawnLocation(): CoreConfig.SpawnLocation {
        val locStr = mainConfig.getString(ConfigConstants.Path.SPAWN_LOCATION, ConfigConstants.DEFAULT_SPAWN_LOCATION)
        val data = ConfigHelper.parseLocationString(locStr, ConfigHelper.LocationData.builder().world("world").x(0.0).y(64.0).z(0.0).yaw(0f).pitch(0f).build())
        return object : CoreConfig.SpawnLocation {
            override fun getWorld() = data.world
            override fun getX() = data.x
            override fun getY() = data.y
            override fun getZ() = data.z
            override fun getYaw() = data.yaw
            override fun getPitch() = data.pitch
        }
    }
}
