package cc.baka9.catseedlogin.velocity.config

import cc.baka9.catseedlogin.common.config.BaseConfigManager
import cc.baka9.catseedlogin.velocity.PluginMain
import java.io.InputStream

class VelocityConfigManager(private val plugin: PluginMain) : BaseConfigManager() {
    init {
        initConfig(plugin.getDataDirectory().toFile(), "config.yml")
    }

    override fun getResource(name: String): InputStream? = javaClass.classLoader.getResourceAsStream(name)
}
