package cc.baka9.catseedlogin.bungee

import cc.baka9.catseedlogin.bungee.config.BungeeConfigManager
import cc.baka9.catseedlogin.common.i18n.MessageKey
import java.util.concurrent.ConcurrentHashMap
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PreLoginEvent
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class Listeners(
    private val configManager: BungeeConfigManager,
    private val communication: BungeeCommunication
) : Listener {
    private val proxyServer = ProxyServer.getInstance()
    private val loggedInPlayerList = ConcurrentHashMap.newKeySet<String>()

    @EventHandler
    fun onChat(event: ChatEvent) {
        if (!configManager.isEnable()) return
        if (!event.isProxyCommand || event.sender !is ProxiedPlayer) return
        val player = event.sender as ProxiedPlayer
        val playerName = player.name
        if (!loggedInPlayerList.contains(playerName)) {
            event.isCancelled = true
            handleLogin(player, event.message)
        }
    }

    @EventHandler
    fun onServerConnect(event: ServerConnectEvent) {
        if (!configManager.isEnable()) return
        if (event.isCancelled) return
        val loginServerName = configManager.getLoginServerName()
        if (event.target.name == loginServerName) return

        val player = event.player
        val playerName = player.name
        if (loggedInPlayerList.contains(playerName)) return
        event.target = proxyServer.getServerInfo(loginServerName)
        PluginMain.runAsync(Runnable {
            try { if (communication.sendConnectRequest(playerName) == 1) loggedInPlayerList.add(playerName) }
            catch (e: Exception) { proxyServer.logger.warning("Login status refresh failed for player: $playerName") }
        })
    }

    @EventHandler
    fun onServerConnected(event: ServerConnectedEvent) {
        if (!configManager.isEnable()) return
        val loginServerName = configManager.getLoginServerName()
        if (event.server.info.name != loginServerName) return
        val player = event.player
        if (loggedInPlayerList.contains(player.name)) {
            PluginMain.runAsync(Runnable { communication.sendKeepLoggedInRequest(player.name) })
        }
    }

    @EventHandler
    fun onPlayerDisconnect(event: PlayerDisconnectEvent) {
        try {
            loggedInPlayerList.remove(event.player.name)
        } catch (e: Exception) {
            proxyServer.logger.severe("移除玩家时出错: ${e.message}")
        }
    }

    @EventHandler
    fun onPreLogin(event: PreLoginEvent) {
        if (!configManager.isEnable()) return
        val playerName = event.connection.name
        try {
            if (loggedInPlayerList.contains(playerName)) {
                event.setCancelReason(TextComponent(MessageKey.ALREADY_LOGGED_IN_PROXY.get()))
                event.isCancelled = true
            }
        } catch (_: Exception) {
            event.setCancelReason(TextComponent(MessageKey.ERROR_PLEASE_RETRY.get()))
            event.isCancelled = true
        }
    }

    private fun handleLogin(player: ProxiedPlayer, message: String?) {
        if (!configManager.isEnable()) return
        val playerName = player.name
        PluginMain.runAsync(Runnable {
            if (communication.sendConnectRequest(playerName) != 1) return@Runnable
            if (!loggedInPlayerList.add(playerName)) return@Runnable
            if (!message.isNullOrEmpty()) {
                proxyServer.pluginManager.dispatchCommand(player, message.substring(1))
            }
        })
    }
}
