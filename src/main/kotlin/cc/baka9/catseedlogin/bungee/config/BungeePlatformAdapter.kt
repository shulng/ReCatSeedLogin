package cc.baka9.catseedlogin.bungee.config

import cc.baka9.catseedlogin.bungee.PluginMain
import cc.baka9.catseedlogin.common.api.PlatformAdapter
import cc.baka9.catseedlogin.common.i18n.I18n
import java.util.concurrent.TimeUnit
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer

class BungeePlatformAdapter(
    private val plugin: PluginMain,
    private val i18n: I18n
) : PlatformAdapter {
    override fun getName(): String = "BungeeCord"

    override fun logInfo(message: String) {
        plugin.logger.info(message)
    }

    override fun logWarn(message: String) {
        plugin.logger.warning(message)
    }

    override fun logError(message: String) {
        plugin.logger.severe(message)
    }

    override fun logError(message: String, throwable: Throwable) {
        plugin.logger.severe(message)
        throwable?.printStackTrace()
    }

    override fun runAsync(task: Runnable) {
        PluginMain.runAsync(task)
    }

    override fun runSync(task: Runnable) {
        task.run()
    }

    override fun runAsyncLater(task: Runnable, delayTicks: Long) {
        ProxyServer.getInstance().scheduler.schedule(
            plugin,
            task,
            delayTicks * 50,
            TimeUnit.MILLISECONDS
        )
    }

    override fun runSyncLater(task: Runnable, delayTicks: Long) {
        runAsyncLater(task, delayTicks)
    }

    override fun runAsyncTimer(task: Runnable, delayTicks: Long, periodTicks: Long) {
        ProxyServer.getInstance().scheduler.schedule(
            plugin,
            task,
            delayTicks * 50,
            periodTicks * 50,
            TimeUnit.MILLISECONDS
        )
    }

    override fun runSyncTimer(task: Runnable, delayTicks: Long, periodTicks: Long) {
        runAsyncTimer(task, delayTicks, periodTicks)
    }

    override fun getI18n(): I18n = i18n

    override fun getPlatformPlayer(name: String): Any? = ProxyServer.getInstance().getPlayer(name)

    override fun isPlayerOnline(name: String): Boolean = ProxyServer.getInstance().getPlayer(name) != null

    override fun kickPlayer(name: String, reason: String) {
        (ProxyServer.getInstance().getPlayer(name) as ProxiedPlayer?)?.disconnect(reason)
    }

    override fun sendMessage(playerName: String, message: String) {
        (ProxyServer.getInstance().getPlayer(playerName) as ProxiedPlayer?)?.sendMessage(message)
    }

    override fun broadcast(message: String) {
        ProxyServer.getInstance().players.forEach { it.sendMessage(message) }
    }
}
