package cc.baka9.catseedlogin.velocity

import cc.baka9.catseedlogin.common.i18n.I18n
import cc.baka9.catseedlogin.velocity.config.VelocityConfigManager
import cc.baka9.catseedlogin.velocity.config.VelocityPlatformAdapter
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.scheduler.ScheduledTask
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import org.slf4j.Logger

@Plugin(
    id = "catseedlogin",
    name = "CatSeedLogin",
    version = "${'$'}{version}",
    description = "CatSeedLogin的Velocity适配版本，提供跨服登录验证功能",
    authors = ["shulng"]
)
class PluginMain @Inject constructor(
    proxyServer: ProxyServer,
    logger: Logger,
    @DataDirectory dataDirectory: Path
) {
    private val proxyServerInstance = proxyServer
    private val loggerInstance = logger
    private val dataDirectoryInstance = dataDirectory
    companion object {
        private lateinit var instance: PluginMain

        @JvmStatic
        fun getInstance(): PluginMain = instance

        @JvmStatic
        fun runAsync(runnable: Runnable): ScheduledTask =
            instance.proxyServerInstance.scheduler.buildTask(instance, runnable).schedule()

        @JvmStatic
        fun runAsyncDelayed(runnable: Runnable, delay: Long, unit: TimeUnit): ScheduledTask =
            instance.proxyServerInstance.scheduler.buildTask(instance, runnable).delay(delay, unit).schedule()
    }

    private lateinit var configManager: VelocityConfigManager
    private lateinit var platformAdapter: VelocityPlatformAdapter
    private lateinit var communication: VelocityCommunication
    private lateinit var listeners: Listeners

    init {
        instance = this
    }

    fun getDataDirectory(): Path = dataDirectoryInstance

    fun getProxyServer(): ProxyServer = proxyServerInstance

    fun getLogger(): Logger = loggerInstance

    @Subscribe
    fun onProxyInitialization(@Suppress("UNUSED_PARAMETER") event: ProxyInitializeEvent) {
        configManager = VelocityConfigManager(this)
        platformAdapter = VelocityPlatformAdapter(this, configManager.getI18n())
        communication = VelocityCommunication(configManager, loggerInstance)
        listeners = Listeners(configManager, communication, proxyServerInstance, loggerInstance)
        configManager.reload()

        proxyServerInstance.eventManager.register(this, listeners)
        val commandMeta = proxyServerInstance.commandManager.metaBuilder("CatSeedLoginVelocity")
            .aliases("cslv")
            .build()
        proxyServerInstance.commandManager.register(commandMeta, Commands(configManager, proxyServerInstance, loggerInstance))
        loggerInstance.info("CatSeedLogin-Velocity has been enabled!")
    }

    fun getConfigManager(): VelocityConfigManager = configManager

    fun getPlatformAdapter(): VelocityPlatformAdapter = platformAdapter

    fun getI18n(): I18n = configManager.getI18n()

    fun getCommunication(): VelocityCommunication = communication

    fun getListeners(): Listeners = listeners
}
