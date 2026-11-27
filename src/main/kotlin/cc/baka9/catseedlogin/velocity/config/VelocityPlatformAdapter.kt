package cc.baka9.catseedlogin.velocity.config

import cc.baka9.catseedlogin.common.api.PlatformAdapter
import cc.baka9.catseedlogin.common.i18n.I18n
import cc.baka9.catseedlogin.velocity.PluginMain
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import java.util.concurrent.TimeUnit
import net.kyori.adventure.text.Component

class VelocityPlatformAdapter(
    private val plugin: PluginMain,
    private val i18n: I18n
) : PlatformAdapter {
    override fun getName(): String = "Velocity"

    override fun logInfo(message: String) {
        plugin.getLogger().info(message)
    }

    override fun logWarn(message: String) {
        plugin.getLogger().warn(message)
    }

    override fun logError(message: String) {
        plugin.getLogger().error(message)
    }

    override fun logError(message: String, throwable: Throwable) {
        plugin.getLogger().error(message, throwable)
    }

    override fun runAsync(task: Runnable) {
        PluginMain.runAsync(task)
    }

    override fun runSync(task: Runnable) {
        task.run()
    }

    override fun runAsyncLater(task: Runnable, delayTicks: Long) {
        plugin.getProxyServer().scheduler.buildTask(plugin, task)
            .delay(delayTicks * 50, TimeUnit.MILLISECONDS)
            .schedule()
    }

    override fun runSyncLater(task: Runnable, delayTicks: Long) {
        runAsyncLater(task, delayTicks)
    }

    override fun runAsyncTimer(task: Runnable, delayTicks: Long, periodTicks: Long) {
        plugin.getProxyServer().scheduler.buildTask(plugin, task)
            .delay(delayTicks * 50, TimeUnit.MILLISECONDS)
            .repeat(periodTicks * 50, TimeUnit.MILLISECONDS)
            .schedule()
    }

    override fun runSyncTimer(task: Runnable, delayTicks: Long, periodTicks: Long) {
        runAsyncTimer(task, delayTicks, periodTicks)
    }

    override fun getI18n(): I18n = i18n

    override fun getPlatformPlayer(name: String): Any? = plugin.getProxyServer().getPlayer(name).orElse(null)

    override fun isPlayerOnline(name: String): Boolean = plugin.getProxyServer().getPlayer(name).isPresent

    override fun kickPlayer(name: String, reason: String) {
        plugin.getProxyServer().getPlayer(name).ifPresent { player ->
            player.disconnect(Component.text(reason))
        }
    }

    override fun sendMessage(playerName: String, message: String) {
        plugin.getProxyServer().getPlayer(playerName).ifPresent { player ->
            player.sendMessage(Component.text(message))
        }
    }

    override fun broadcast(message: String) {
        val proxyServer: ProxyServer = plugin.getProxyServer()
        proxyServer.allPlayers.forEach { player: Player -> player.sendMessage(Component.text(message)) }
    }
}
