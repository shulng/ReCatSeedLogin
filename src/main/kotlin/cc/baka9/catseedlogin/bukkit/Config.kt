package cc.baka9.catseedlogin.bukkit

import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager
import cc.baka9.catseedlogin.bukkit.util.WorldUtil
import cc.baka9.catseedlogin.common.config.ConfigConstants
import cc.baka9.catseedlogin.common.i18n.MessageKey
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.Optional
import java.util.regex.Pattern

object Config {
    private lateinit var plugin: CatSeedLogin

    class MySQL {
        companion object {
            @JvmField @Volatile var Enable = false
            @JvmField @Volatile var Host = ""
            @JvmField @Volatile var Port = ""
            @JvmField @Volatile var Database = ""
            @JvmField @Volatile var User = ""
            @JvmField @Volatile var Password = ""

            @JvmStatic fun load() {
                val manager = plugin.getConfigManager()
                Enable = manager.isMySQL()
                Host = manager.getDatabaseHost()
                Port = manager.getDatabasePort().toString()
                Database = manager.getDatabaseName()
                User = manager.getDatabaseUser()
                Password = manager.getDatabasePassword()
            }
        }
    }

    class BungeeCord {
        companion object {
            @JvmField @Volatile var Enable = false
            @JvmField @Volatile var Host = ""
            @JvmField @Volatile var Port = ""
            @JvmField @Volatile var AuthKey = ""

            @JvmStatic fun load() {
                val manager = plugin.getConfigManager()
                Enable = manager.isEnable()
                Host = manager.getProxyHost()
                Port = manager.getProxyPort().toString()
                AuthKey = manager.getAuthKey()
            }
        }
    }

    class Settings {
        companion object {
            @JvmField @Volatile var IpRegisterCountLimit = 0
            @JvmField @Volatile var IpCountLimit = 0
            @JvmField @Volatile var SpawnLocation: Location? = null
            @JvmField @Volatile var LimitChineseID = false
            @JvmField @Volatile var BedrockLoginBypass = false
            @JvmField @Volatile var LoginwiththesameIP = false
            @JvmField @Volatile var EmptyBackpack = false
            @JvmField @Volatile var IPTimeout = 0
            @JvmField @Volatile var MaxLengthID = 0
            @JvmField @Volatile var MinLengthID = 0
            @JvmField @Volatile var BeforeLoginNoDamage = false
            @JvmField @Volatile var ReenterInterval = 0L
            @JvmField @Volatile var AfterLoginBack = false
            @JvmField @Volatile var CanTpSpawnLocation = false
            @JvmField @Volatile var CommandWhiteList: List<Pattern> = emptyList()
            @JvmField @Volatile var AutoKick = 0
            @JvmField @Volatile var NamePattern = ""
            @JvmField @Volatile var DeathStateQuitRecordLocation = false
            @JvmField @Volatile var FloodgatePrefixProtect = false
            @JvmField @Volatile var LoopbackLoginBypass = false
            @JvmField @Volatile var BeforeLoginAllowChat = false
            @JvmField @Volatile var BlindingBeforeLogin = false
            @JvmField @Volatile var TwoFactorEnabled = false
            @JvmField @Volatile var TwoFactorIssuer = ""
            @JvmField @Volatile var TwoFactorDigits = 0
            @JvmField @Volatile var TwoFactorPeriod = 0
            @JvmField @Volatile var TwoFactorRecoveryCodeCount = 0
            @JvmField @Volatile var TwoFactorMaxAttempts = 0
            @JvmField @Volatile var TwoFactorAttemptWindow = 0
            @JvmField @Volatile var TwoFactorPendingTimeout = 0

            @JvmStatic fun load() {
                val manager = plugin.getConfigManager()
                IpRegisterCountLimit = manager.getIpRegisterCountLimit()
                IpCountLimit = manager.getIpCountLimit()
                LimitChineseID = manager.isLimitChineseID()
                MinLengthID = manager.getMinLengthID()
                BedrockLoginBypass = manager.isBedrockLoginBypass()
                LoginwiththesameIP = manager.isLoginWithSameIP()
                EmptyBackpack = manager.isEmptyBackpack()
                MaxLengthID = manager.getMaxLengthID()
                BeforeLoginNoDamage = manager.isBeforeLoginNoDamage()
                ReenterInterval = manager.getReenterInterval()
                AfterLoginBack = manager.isAfterLoginBack()
                CanTpSpawnLocation = manager.isCanTpSpawnLocation()
                NamePattern = manager.getNamePattern()
                CommandWhiteList = manager.getCommandWhiteList()
                AutoKick = manager.getAutoKick()
                IPTimeout = manager.getIPTimeout()
                SpawnLocation = manager.getBukkitSpawnLocation()
                DeathStateQuitRecordLocation = manager.isDeathStateQuitRecordLocation()
                FloodgatePrefixProtect = manager.isFloodgatePrefixProtect()
                LoopbackLoginBypass = manager.isLoopbackLoginBypass()
                BeforeLoginAllowChat = manager.isBeforeLoginAllowChat()
                BlindingBeforeLogin = manager.isBlindingBeforeLogin()
                TwoFactorEnabled = manager.isTwoFactorEnabled()
                TwoFactorIssuer = manager.getTwoFactorIssuer()
                TwoFactorDigits = manager.getTwoFactorDigits()
                TwoFactorPeriod = manager.getTwoFactorPeriod()
                TwoFactorRecoveryCodeCount = manager.getTwoFactorRecoveryCodeCount()
                TwoFactorMaxAttempts = manager.getTwoFactorMaxAttempts()
                TwoFactorAttemptWindow = manager.getTwoFactorAttemptWindow()
                TwoFactorPendingTimeout = manager.getTwoFactorPendingTimeout()
            }

            @JvmStatic fun save() {
                val manager = plugin.getConfigManager()
                manager.set(ConfigConstants.Path.SETTINGS_LOGIN_IP_REGISTER_LIMIT, IpRegisterCountLimit)
                manager.set(ConfigConstants.Path.SETTINGS_LOGIN_IP_COUNT_LIMIT, IpCountLimit)
                manager.set(ConfigConstants.Path.SETTINGS_ACCOUNT_LIMIT_CHINESE_ID, LimitChineseID)
                manager.set(ConfigConstants.Path.BEDROCK_LOGIN_BYPASS, BedrockLoginBypass)
                manager.set(ConfigConstants.Path.SETTINGS_LOGIN_SAME_IP_ENABLED, LoginwiththesameIP)
                manager.set(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_EMPTY_BACKPACK, EmptyBackpack)
                manager.set(ConfigConstants.Path.SETTINGS_LOGIN_SAME_IP_TIMEOUT, IPTimeout)
                manager.set(ConfigConstants.Path.SETTINGS_ACCOUNT_MIN_LENGTH_ID, MinLengthID)
                manager.set(ConfigConstants.Path.SETTINGS_ACCOUNT_MAX_LENGTH_ID, MaxLengthID)
                manager.set(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_NO_DAMAGE, BeforeLoginNoDamage)
                manager.set(ConfigConstants.Path.SETTINGS_LOGIN_REENTER_INTERVAL, ReenterInterval)
                manager.set(ConfigConstants.Path.SETTINGS_LOGIN_AFTER_LOGIN_BACK, AfterLoginBack)
                manager.set(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_CAN_TP_SPAWN, CanTpSpawnLocation)
                manager.set(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_AUTO_KICK, AutoKick)
                manager.set(ConfigConstants.Path.SETTINGS_LOGIN_DEATH_STATE_QUIT_RECORD, DeathStateQuitRecordLocation)
                manager.set(ConfigConstants.Path.BEDROCK_FLOODGATE_PREFIX, FloodgatePrefixProtect)
                manager.set(ConfigConstants.Path.SETTINGS_ACCOUNT_NAME_PATTERN, NamePattern)
                manager.set(ConfigConstants.Path.SETTINGS_LOGIN_LOOPBACK_BYPASS, LoopbackLoginBypass)
                manager.set(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_ALLOW_CHAT, BeforeLoginAllowChat)
                manager.set(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_BLINDING, BlindingBeforeLogin)
                manager.set(ConfigConstants.Path.TWO_FACTOR_ENABLED, TwoFactorEnabled)
                manager.set(ConfigConstants.Path.TWO_FACTOR_ISSUER, TwoFactorIssuer)
                manager.set(ConfigConstants.Path.TWO_FACTOR_DIGITS, TwoFactorDigits)
                manager.set(ConfigConstants.Path.TWO_FACTOR_PERIOD, TwoFactorPeriod)
                manager.set(ConfigConstants.Path.TWO_FACTOR_RECOVERY_CODE_COUNT, TwoFactorRecoveryCodeCount)
                manager.set(ConfigConstants.Path.TWO_FACTOR_MAX_ATTEMPTS, TwoFactorMaxAttempts)
                manager.set(ConfigConstants.Path.TWO_FACTOR_ATTEMPT_WINDOW, TwoFactorAttemptWindow)
                manager.set(ConfigConstants.Path.TWO_FACTOR_PENDING_TIMEOUT, TwoFactorPendingTimeout)

                if (CommandWhiteList.isNotEmpty()) {
                    manager.getMainConfig().set(
                        ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_COMMAND_WHITELIST,
                        CommandWhiteList.map(Pattern::toString)
                    )
                }
                SpawnLocation?.let(manager::setSpawnLocation)
            }
        }
    }

    class Language {
        companion object {
            @JvmField var LOGIN_REQUEST = ""
            @JvmField var REGISTER_REQUEST = ""
            @JvmField var LOGIN_NOREGISTER = ""
            @JvmField var LOGIN_REPEAT = ""
            @JvmField var LOGIN_SUCCESS = ""
            @JvmField var LOGIN_FAIL = ""
            @JvmField var LOGIN_FAIL_IF_FORGET = ""
            @JvmField var REGISTER_SUCCESS = ""
            @JvmField var REGISTER_BEFORE_LOGIN_ALREADY = ""
            @JvmField var REGISTER_AFTER_LOGIN_ALREADY = ""
            @JvmField var REGISTER_PASSWORD_CONFIRM_FAIL = ""
            @JvmField var COMMON_PASSWORD_SO_SIMPLE = ""
            @JvmField var RESETPASSWORD_NOREGISTER = ""
            @JvmField var RESETPASSWORD_EMAIL_DISABLE = ""
            @JvmField var RESETPASSWORD_EMAIL_NO_SET = ""
            @JvmField var RESETPASSWORD_EMAIL_REPEAT_SEND_MESSAGE = ""
            @JvmField var RESETPASSWORD_EMAIL_SENDING_MESSAGE = ""
            @JvmField var RESETPASSWORD_EMAIL_SENT_MESSAGE = ""
            @JvmField var RESETPASSWORD_EMAIL_WARN = ""
            @JvmField var RESETPASSWORD_SUCCESS = ""
            @JvmField var RESETPASSWORD_EMAILCODE_INCORRECT = ""
            @JvmField var RESETPASSWORD_FAIL = ""
            @JvmField var CHANGEPASSWORD_NOREGISTER = ""
            @JvmField var CHANGEPASSWORD_NOLOGIN = ""
            @JvmField var CHANGEPASSWORD_OLDPASSWORD_INCORRECT = ""
            @JvmField var CHANGEPASSWORD_PASSWORD_CONFIRM_FAIL = ""
            @JvmField var CHANGEPASSWORD_SUCCESS = ""
            @JvmField var AUTO_KICK = ""
            @JvmField var REGISTER_MORE = ""
            @JvmField var BEDROCK_LOGIN_BYPASS = ""
            @JvmField var LOGIN_WITH_THE_SAME_IP = ""
            @JvmField var LOOPBACK_LOGIN_BYPASS = ""

            @JvmStatic fun load() {
                LOGIN_REQUEST = MessageKey.LOGIN_REQUEST.get()
                REGISTER_REQUEST = MessageKey.REGISTER_REQUEST.get()
                LOGIN_NOREGISTER = MessageKey.LOGIN_NOREGISTER.get()
                LOGIN_REPEAT = MessageKey.LOGIN_REPEAT.get()
                LOGIN_SUCCESS = MessageKey.LOGIN_SUCCESS.get()
                LOGIN_FAIL = MessageKey.LOGIN_FAIL.get()
                LOGIN_FAIL_IF_FORGET = MessageKey.LOGIN_FAIL_IF_FORGET.get()
                REGISTER_SUCCESS = MessageKey.REGISTER_SUCCESS.get()
                REGISTER_BEFORE_LOGIN_ALREADY = MessageKey.REGISTER_BEFORE_LOGIN_ALREADY.get()
                REGISTER_AFTER_LOGIN_ALREADY = MessageKey.REGISTER_AFTER_LOGIN_ALREADY.get()
                REGISTER_PASSWORD_CONFIRM_FAIL = MessageKey.REGISTER_PASSWORD_CONFIRM_FAIL.get()
                COMMON_PASSWORD_SO_SIMPLE = MessageKey.COMMON_PASSWORD_SO_SIMPLE.get()
                RESETPASSWORD_NOREGISTER = MessageKey.RESETPASSWORD_NOREGISTER.get()
                RESETPASSWORD_EMAIL_DISABLE = MessageKey.RESETPASSWORD_EMAIL_DISABLE.get()
                RESETPASSWORD_EMAIL_NO_SET = MessageKey.RESETPASSWORD_EMAIL_NO_SET.get()
                RESETPASSWORD_EMAIL_REPEAT_SEND_MESSAGE = MessageKey.RESETPASSWORD_EMAIL_REPEAT_SEND_MESSAGE.get()
                RESETPASSWORD_EMAIL_SENDING_MESSAGE = MessageKey.RESETPASSWORD_EMAIL_SENDING_MESSAGE.get()
                RESETPASSWORD_EMAIL_SENT_MESSAGE = MessageKey.RESETPASSWORD_EMAIL_SENT_MESSAGE.get()
                RESETPASSWORD_EMAIL_WARN = MessageKey.RESETPASSWORD_EMAIL_WARN.get()
                RESETPASSWORD_SUCCESS = MessageKey.RESETPASSWORD_SUCCESS.get()
                RESETPASSWORD_EMAILCODE_INCORRECT = MessageKey.RESETPASSWORD_EMAILCODE_INCORRECT.get()
                RESETPASSWORD_FAIL = MessageKey.RESETPASSWORD_FAIL.get()
                CHANGEPASSWORD_NOREGISTER = MessageKey.CHANGEPASSWORD_NOREGISTER.get()
                CHANGEPASSWORD_NOLOGIN = MessageKey.CHANGEPASSWORD_NOLOGIN.get()
                CHANGEPASSWORD_OLDPASSWORD_INCORRECT = MessageKey.CHANGEPASSWORD_OLDPASSWORD_INCORRECT.get()
                CHANGEPASSWORD_PASSWORD_CONFIRM_FAIL = MessageKey.CHANGEPASSWORD_PASSWORD_CONFIRM_FAIL.get()
                CHANGEPASSWORD_SUCCESS = MessageKey.CHANGEPASSWORD_SUCCESS.get()
                AUTO_KICK = MessageKey.AUTO_KICK.get()
                REGISTER_MORE = MessageKey.REGISTER_MORE.get()
                BEDROCK_LOGIN_BYPASS = MessageKey.BEDROCK_LOGIN_BYPASS.get()
                LOGIN_WITH_THE_SAME_IP = MessageKey.LOGIN_WITH_THE_SAME_IP.get()
                LOOPBACK_LOGIN_BYPASS = MessageKey.LOOPBACK_LOGIN_BYPASS.get()
            }
        }
    }

    class EmailVerify {
        companion object {
            @JvmField @Volatile var Enable = false
            @JvmField @Volatile var EmailAccount = ""
            @JvmField @Volatile var EmailPassword = ""
            @JvmField @Volatile var EmailSmtpHost = ""
            @JvmField @Volatile var EmailSmtpPort = ""
            @JvmField @Volatile var SSLAuthVerify = false
            @JvmField @Volatile var FromPersonal = ""

            @JvmStatic fun load() {
                val manager = plugin.getConfigManager()
                Enable = manager.isEmailEnable()
                EmailAccount = manager.getEmailAccount()
                EmailPassword = manager.getEmailPassword()
                EmailSmtpHost = manager.getEmailSmtpHost()
                EmailSmtpPort = manager.getEmailSmtpPort()
                SSLAuthVerify = manager.isSSLAuthVerify()
                FromPersonal = manager.getFromPersonal()
            }
        }
    }

    @JvmStatic fun load() {
        plugin = CatSeedLogin.instance
        plugin.getConfigManager().createDefaultConfig("config.yml")
        MySQL.load()
        Settings.load()
        EmailVerify.load()
        Language.load()
        BungeeCord.load()
    }

    @JvmStatic fun save() = Settings.save()

    @JvmStatic fun reload() {
        plugin.getConfigManager().reload()
        load()
    }

    @JvmStatic fun getOfflineLocation(player: Player): Optional<Location> = try {
        val location = CatSeedLogin.sql.getLocation(player.name)
        if (!location.isNullOrEmpty()) Optional.of(parseLocation(location)) else Optional.empty()
    } catch (exception: Exception) {
        plugin.logger.warning("获取玩家离线位置失败: ${player.name}")
        exception.printStackTrace()
        Optional.empty()
    }

    @JvmStatic fun setOfflineLocation(player: Player) {
        val location = locationToString(player.location)
        plugin.runTaskAsync {
            try {
                CatSeedLogin.sql.updateLocation(player.name, location)
            } catch (exception: Exception) {
                plugin.logger.warning("保存玩家离线位置失败: ${player.name}")
                exception.printStackTrace()
            }
        }
    }

    @JvmStatic fun setOfflineLocationSync(player: Player) {
        val location = locationToString(player.location)
        try {
            CatSeedLogin.sql.updateLocation(player.name, location)
        } catch (exception: Exception) {
            plugin.logger.warning("保存玩家离线位置失败: ${player.name}")
            exception.printStackTrace()
        }
    }

    private fun parseLocation(value: String): Location {
        try {
            val parts = value.split(":")
            if (parts.size < 6) return defaultSpawnLocation()
            val world = Bukkit.getWorld(parts[0]) ?: WorldUtil.getDefaultWorld(plugin.logger)
                ?: return defaultSpawnLocation()
            return Location(
                world,
                parts[1].toDouble(),
                parts[2].toDouble(),
                parts[3].toDouble(),
                parts[4].toFloat(),
                parts[5].toFloat()
            )
        } catch (_: NumberFormatException) {
            return defaultSpawnLocation()
        }
    }

    private fun defaultSpawnLocation(): Location {
        val world: World? = WorldUtil.getDefaultWorld(plugin.logger)
        return world?.spawnLocation ?: Bukkit.getWorlds()[0].spawnLocation
    }

    private fun locationToString(location: Location): String = try {
        String.format(
            "%s:%.2f:%.2f:%.2f:%.2f:%.2f",
            location.world.name,
            location.x,
            location.y,
            location.z,
            location.yaw,
            location.pitch
        )
    } catch (exception: Exception) {
        exception.printStackTrace()
        val defaultLocation = WorldUtil.getDefaultWorld(plugin.logger)!!.spawnLocation
        String.format(
            "%s:%.2f:%.2f:%.2f:%.2f:%.2f",
            defaultLocation.world.name,
            defaultLocation.x,
            defaultLocation.y,
            defaultLocation.z,
            defaultLocation.yaw,
            defaultLocation.pitch
        )
    }
}
