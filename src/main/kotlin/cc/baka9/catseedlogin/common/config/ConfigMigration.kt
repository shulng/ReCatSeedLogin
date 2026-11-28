package cc.baka9.catseedlogin.common.config

import java.util.LinkedHashMap
import java.util.logging.Level
import java.util.logging.Logger

object ConfigMigration {
    private val LOGGER = Logger.getLogger(ConfigMigration::class.java.name)
    private val LEGACY_PATHS: LinkedHashMap<String, String> = linkedMapOf(
        "settings.ip-register-count-limit" to ConfigConstants.Path.SETTINGS_LOGIN_IP_REGISTER_LIMIT,
        "settings.ip-count-limit" to ConfigConstants.Path.SETTINGS_LOGIN_IP_COUNT_LIMIT,
        "settings.death-state-quit-record-location" to ConfigConstants.Path.SETTINGS_LOGIN_DEATH_STATE_QUIT_RECORD,
        "same-ip-login.enabled" to ConfigConstants.Path.SETTINGS_LOGIN_SAME_IP_ENABLED,
        "same-ip-login.timeout" to ConfigConstants.Path.SETTINGS_LOGIN_SAME_IP_TIMEOUT,
        "settings.limit-chinese-id" to ConfigConstants.Path.SETTINGS_ACCOUNT_LIMIT_CHINESE_ID,
        "settings.min-length-id" to ConfigConstants.Path.SETTINGS_ACCOUNT_MIN_LENGTH_ID,
        "settings.max-length-id" to ConfigConstants.Path.SETTINGS_ACCOUNT_MAX_LENGTH_ID,
        "settings.name-pattern" to ConfigConstants.Path.SETTINGS_ACCOUNT_NAME_PATTERN,
        "settings.before-login-no-damage" to ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_NO_DAMAGE,
        "settings.can-tp-spawn-location" to ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_CAN_TP_SPAWN,
        "settings.before-login-allow-chat" to ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_ALLOW_CHAT,
        "settings.blinding-before-login" to ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_BLINDING,
        "empty-backpack" to ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_EMPTY_BACKPACK,
        "settings.auto-kick" to ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_AUTO_KICK,
        "settings.command-white-list" to ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_COMMAND_WHITELIST,
        "settings.loopback-login-bypass" to ConfigConstants.Path.SETTINGS_LOGIN_LOOPBACK_BYPASS,
        "settings.reenter-interval" to ConfigConstants.Path.SETTINGS_LOGIN_REENTER_INTERVAL,
        "settings.after-login-back" to ConfigConstants.Path.SETTINGS_LOGIN_AFTER_LOGIN_BACK
    )
    private val LEGACY_SECTIONS = arrayOf("same-ip-login", "settings")

    @JvmStatic fun migrate(config: YamlConfiguration): Boolean {
        val legacyValues = readLegacyValues(config)
        if (legacyValues.isEmpty()) return removeEmptySections(config)
        legacyValues.keys.forEach { config.set(it, null) }
        var moved = 0
        LEGACY_PATHS.forEach { (oldPath, newPath) ->
            if (legacyValues.containsKey(oldPath) && !config.contains(newPath)) {
                config.set(newPath, legacyValues[oldPath]); moved++
            }
        }
        val changed = removeEmptySections(config) || moved > 0
        LOGGER.log(Level.INFO, "已将 {0} 个旧配置路径迁移到新结构，旧路径已自动移除。", moved)
        return changed
    }

    private fun readLegacyValues(config: YamlConfiguration): Map<String, Any?> = buildMap {
        LEGACY_PATHS.keys.forEach { if (config.contains(it)) put(it, config.get(it)) }
    }

    private fun removeEmptySections(config: YamlConfiguration): Boolean {
        val root = config.getDataMap()
        var changed = false
        LEGACY_SECTIONS.forEach { section ->
            if (root[section] is Map<*, *> && (root[section] as Map<*, *>).isEmpty()) { root.remove(section); changed = true }
        }
        return changed
    }
}
