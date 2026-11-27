package cc.baka9.catseedlogin.velocity

import cc.baka9.catseedlogin.common.i18n.MessageKey
import cc.baka9.catseedlogin.common.util.TabCompleteUtil
import cc.baka9.catseedlogin.velocity.config.VelocityConfigManager
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.ProxyServer
import java.util.concurrent.CompletableFuture
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.slf4j.Logger

class Commands(
    private val configManager: VelocityConfigManager,
    private val proxyServer: ProxyServer,
    private val logger: Logger
) : SimpleCommand {
    companion object {
        const val PERMISSION = "catseedlogin.admin"

        @JvmField
        val SUB_COMMANDS: List<String> = listOf("reload", "status", "list")
    }

    override fun execute(invocation: SimpleCommand.Invocation) {
        val source = invocation.source()
        val args = invocation.arguments()
        try {
            if (!source.hasPermission(PERMISSION)) {
                source.sendMessage(Component.text(MessageKey.NO_PERMISSION.get()))
                return
            }
            if (args.isEmpty()) {
                sendHelp(source)
                return
            }
            when (args[0].lowercase()) {
                "reload" -> handleReload(source)
                "status" -> handleStatus(source)
                "list" -> handleList(source)
                else -> sendHelp(source)
            }
        } catch (e: Exception) {
            source.sendMessage(Component.text(MessageKey.INTERNAL_ERROR.get(), NamedTextColor.RED))
            logger.error("Error executing command", e)
        }
    }

    override fun suggestAsync(invocation: SimpleCommand.Invocation): CompletableFuture<List<String>> {
        val source = invocation.source()
        if (!source.hasPermission(PERMISSION)) return CompletableFuture.completedFuture(emptyList())
        val args = invocation.arguments()
        if (args.size <= 1) {
            return CompletableFuture.completedFuture(TabCompleteUtil.filter(SUB_COMMANDS, TabCompleteUtil.lastArg(args)))
        }
        return CompletableFuture.completedFuture(emptyList())
    }

    private fun sendHelp(source: CommandSource) {
        source.sendMessage(Component.text("=== CatSeedLogin-Velocity ===", NamedTextColor.GOLD))
        source.sendMessage(Component.text("/cslv reload", NamedTextColor.YELLOW))
        source.sendMessage(Component.text("/cslv status", NamedTextColor.YELLOW))
        source.sendMessage(Component.text("/cslv list", NamedTextColor.YELLOW))
    }

    private fun handleReload(source: CommandSource) {
        try {
            configManager.reload()
            source.sendMessage(Component.text(MessageKey.CONFIG_RELOADED.get()))
        } catch (e: Exception) {
            source.sendMessage(Component.text(MessageKey.INTERNAL_ERROR.get(), NamedTextColor.RED))
            logger.error("Failed to reload config", e)
        }
    }

    private fun handleStatus(source: CommandSource) {
        try {
            source.sendMessage(Component.text("=== CatSeedLogin-Velocity ===", NamedTextColor.GOLD))
            val host = configManager.getProxyHost()
            val port = configManager.getProxyPort()
            val loginServerName = configManager.getLoginServerName()
            source.sendMessage(Component.text("$host:$port", NamedTextColor.YELLOW))
            source.sendMessage(Component.text(loginServerName, NamedTextColor.YELLOW))
            val loginServerOnline = proxyServer.getServer(loginServerName).isPresent
            source.sendMessage(
                Component.text(
                    if (loginServerOnline) "Online" else "Offline",
                    if (loginServerOnline) NamedTextColor.GREEN else NamedTextColor.RED
                )
            )
        } catch (e: Exception) {
            source.sendMessage(Component.text(MessageKey.INTERNAL_ERROR.get(), NamedTextColor.RED))
            logger.error("Error getting status", e)
        }
    }

    private fun handleList(source: CommandSource) {
        try {
            val loggedInPlayers = PluginMain.getInstance().getListeners().getLoggedInPlayers()
            source.sendMessage(Component.text("=== ${loggedInPlayers.size} ===", NamedTextColor.GOLD))
            displayPlayerList(source, loggedInPlayers)
        } catch (e: Exception) {
            source.sendMessage(Component.text(MessageKey.INTERNAL_ERROR.get(), NamedTextColor.RED))
            logger.error("Error getting player list", e)
        }
    }

    private fun displayPlayerList(source: CommandSource, loggedInPlayers: List<String>) {
        if (loggedInPlayers.isEmpty()) {
            source.sendMessage(Component.text("None", NamedTextColor.GRAY))
            return
        }
        loggedInPlayers.forEach { playerName ->
            source.sendMessage(Component.text("- $playerName", NamedTextColor.WHITE))
        }
    }
}
