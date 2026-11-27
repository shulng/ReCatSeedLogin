package cc.baka9.catseedlogin.bungee

import cc.baka9.catseedlogin.bungee.config.BungeeConfigManager
import cc.baka9.catseedlogin.bungee.config.BungeePlatformAdapter
import cc.baka9.catseedlogin.common.config.PluginContext
import cc.baka9.catseedlogin.common.i18n.I18n
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.scheduler.ScheduledTask

class PluginMain : Plugin(), PluginContext {
    companion object {
        @JvmField
        var instance: PluginMain? = null

        @JvmStatic
        fun runAsync(runnable: Runnable): ScheduledTask =
            (instance ?: error("Bungee plugin has not been enabled yet")).let { plugin ->
                plugin.proxy.scheduler.runAsync(plugin, runnable)
            }
    }

    private lateinit var configManager: BungeeConfigManager
    private lateinit var platformAdapter: BungeePlatformAdapter
    private lateinit var communication: BungeeCommunication

    override fun onEnable() {
        instance = this
        configManager = BungeeConfigManager(this)
        platformAdapter = BungeePlatformAdapter(this, configManager.getI18n())
        communication = BungeeCommunication(configManager, logger)
        configManager.reload()
        proxy.pluginManager.registerListener(this, Listeners(configManager, communication))
        proxy.pluginManager.registerCommand(
            this,
            BungeeCommands("CatSeedLoginBungee", "catseedlogin.admin", configManager, "cslb")
        )
    }

    fun getConfigManager(): BungeeConfigManager = configManager

    fun getPlatformAdapter(): BungeePlatformAdapter = platformAdapter

    fun getI18n(): I18n = configManager.getI18n()

    fun getCommunication(): BungeeCommunication = communication
}
