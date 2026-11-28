package cc.baka9.catseedlogin.velocity

import cc.baka9.catseedlogin.common.i18n.MessageKey
import cc.baka9.catseedlogin.velocity.config.VelocityConfigManager
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import com.velocitypowered.api.event.player.ServerPreConnectEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import net.kyori.adventure.text.Component
import org.slf4j.Logger

class Listeners(
    private val configManager: VelocityConfigManager,
    private val communication: VelocityCommunication,
    private val proxyServer: ProxyServer,
    private val logger: Logger
) {
    private val loggedInPlayerList = ConcurrentHashMap.newKeySet<String>()

    @Subscribe
    fun onChat(event: PlayerChatEvent) {
        if (!configManager.isEnable()) return
        val player = event.player
        val message = event.message
        if (message.startsWith("/") && isNotLoggedIn(player)) {
            event.result = PlayerChatEvent.ChatResult.denied()
            handleLogin(player, message)
        }
    }

    @Subscribe
    fun onCommandExecute(event: com.velocitypowered.api.event.command.CommandExecuteEvent) {
        if (!configManager.isEnable()) return
        val source = event.commandSource
        if (source !is Player) return
        val command = event.command
        if (isNotLoggedIn(source) && !isCommandAllowed("/$command")) {
            event.result = com.velocitypowered.api.event.command.CommandExecuteEvent.CommandResult.denied()
            handleLogin(source, "/$command")
        }
    }

    private fun isCommandAllowed(input: String): Boolean =
        configManager.getCommandWhiteList().any { it.matcher(input).matches() }

    @Subscribe
    fun onServerPreConnect(event: ServerPreConnectEvent) {
        if (!configManager.isEnable()) return
        val player = event.player
        val target: RegisteredServer = event.result.server.orElse(null) ?: return
        val playerName = player.username
        val loginServerName = configManager.getLoginServerName()
        if (loggedInPlayerList.contains(playerName)) return
        if (target.serverInfo.name == loginServerName) {
            handleLogin(player, null)
            return
        }
        redirectToLoginServer(loginServerName, event)
        PluginMain.runAsync(Runnable {
            try { if (communication.sendConnectRequest(playerName) == 1) loggedInPlayerList.add(playerName) }
            catch (e: Exception) { logger.debug("Login status refresh failed for player: $playerName", e) }
        })
    }

    private fun redirectToLoginServer(loginServerName: String, event: ServerPreConnectEvent) {
        proxyServer.getServer(loginServerName).ifPresent { loginServer ->
            event.result = ServerPreConnectEvent.ServerResult.allowed(loginServer)
        }
    }

    @Subscribe
    fun onServerConnected(event: ServerConnectedEvent) {
        if (!configManager.isEnable()) return
        val player = event.player
        val serverName = event.server.serverInfo.name
        val loginServerName = configManager.getLoginServerName()
        if (serverName == loginServerName && loggedInPlayerList.contains(player.username)) {
            PluginMain.runAsyncDelayed(
                Runnable { communication.sendKeepLoggedInRequest(player.username) },
                1,
                TimeUnit.SECONDS
            )
        }
    }

    @Subscribe
    fun onPlayerDisconnect(event: DisconnectEvent) {
        val player = event.player
        try {
            loggedInPlayerList.remove(player.username)
        } catch (_: Exception) {
            logger.warn("Failed to remove player from logged-in list: ${player.username}")
        }
    }

    @Subscribe
    fun onPreLogin(event: PreLoginEvent) {
        if (!configManager.isEnable()) return
        val playerName = event.username
        try {
            if (loggedInPlayerList.contains(playerName)) {
                event.result = PreLoginEvent.PreLoginComponentResult.denied(
                    Component.text(MessageKey.ALREADY_LOGGED_IN_PROXY.get())
                )
            }
        } catch (_: Exception) {
            event.result = PreLoginEvent.PreLoginComponentResult.denied(
                Component.text(MessageKey.ERROR_PLEASE_RETRY.get())
            )
        }
    }

    private fun isNotLoggedIn(player: Player): Boolean = !loggedInPlayerList.contains(player.username)

    private fun handleLogin(player: Player, message: String?) {
        if (!configManager.isEnable()) return
        val playerName = player.username
        PluginMain.runAsync(Runnable { handleLoginAsync(player, playerName, message) })
    }

    private fun handleLoginAsync(player: Player, playerName: String, message: String?) {
        try {
            if (communication.sendConnectRequest(playerName) != 1) return
            if (!loggedInPlayerList.add(playerName)) return
            executeQueuedCommand(player, message)
        } catch (e: Exception) {
            logger.error("Error handling login for player: $playerName", e)
        }
    }

    private fun executeQueuedCommand(player: Player, message: String?) {
        if (message == null || !message.startsWith("/")) return
        proxyServer.commandManager.executeAsync(player, message.substring(1))
    }

    fun getLoggedInPlayers(): List<String> = loggedInPlayerList.toList()
}
