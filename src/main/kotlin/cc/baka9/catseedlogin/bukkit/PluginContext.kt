package cc.baka9.catseedlogin.bukkit

import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager
import cc.baka9.catseedlogin.bukkit.database.SQL
import java.util.logging.Logger

class PluginContext private constructor(
    private val pluginInstance: CatSeedLogin,
    private var database: SQL,
    private val protocolLibLoaded: Boolean
) {
    companion object {
        @Volatile
        private var instance: PluginContext? = null

        @JvmStatic
        fun init(plugin: CatSeedLogin, sql: SQL, loadProtocolLib: Boolean) {
            instance = PluginContext(plugin, sql, loadProtocolLib)
        }

        @JvmStatic
        fun get(): PluginContext = instance ?: error("PluginContext has not been initialized")

        @JvmStatic
        fun getSql(): SQL = get().database

        @JvmStatic
        fun setSql(sql: SQL) {
            get().database = sql
            CatSeedLogin.sql = sql
        }

        @JvmStatic
        fun isLoadProtocolLib(): Boolean = get().protocolLibLoaded

        @JvmStatic
        fun getLogger(): Logger = get().pluginInstance.logger

        @JvmStatic
        fun getConfigManager(): BukkitConfigManager = get().pluginInstance.getConfigManager()

        @JvmStatic
        fun getPlugin(): CatSeedLogin = get().pluginInstance
    }
}
