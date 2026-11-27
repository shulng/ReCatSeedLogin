package cc.baka9.catseedlogin.bungee.config

import cc.baka9.catseedlogin.common.config.BaseConfigManager
import cc.baka9.catseedlogin.common.config.PluginContext
import java.io.InputStream

class BungeeConfigManager(private val plugin: PluginContext) : BaseConfigManager() {
    init {
        initConfig(plugin.getDataFolder(), "config.yml")
    }

    override fun getResource(name: String): InputStream? = plugin.getResourceAsStream(name)
}
