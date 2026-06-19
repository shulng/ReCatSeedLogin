package cc.baka9.catseedlogin.velocity;

import cc.baka9.catseedlogin.velocity.config.VelocityConfigManager;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class Listeners {

    private final List<String> loggedInPlayerList = new CopyOnWriteArrayList<>();
    private final VelocityConfigManager configManager;
    private final VelocityCommunication communication;
    private final ProxyServer proxyServer;
    private final Logger logger;

    public Listeners(VelocityConfigManager configManager, VelocityCommunication communication,
                     ProxyServer proxyServer, Logger logger) {
        this.configManager = configManager;
        this.communication = communication;
        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @Subscribe
    public void onChat(com.velocitypowered.api.event.player.PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        if (message.startsWith("/") && isNotLoggedIn(player)) {
            event.setResult(com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult.denied());
            handleLogin(player, message);
        }
    }

    @Subscribe
    public void onCommandExecute(com.velocitypowered.api.event.command.CommandExecuteEvent event) {
        if (!(event.getCommandSource() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getCommandSource();
        String command = event.getCommand();
        
        if (isNotLoggedIn(player) && 
            !command.toLowerCase().startsWith("login") && 
            !command.toLowerCase().startsWith("register") &&
            !command.toLowerCase().startsWith("l") &&
            !command.toLowerCase().startsWith("reg") &&
            !command.toLowerCase().startsWith("cslv")) {
            
            event.setResult(com.velocitypowered.api.event.command.CommandExecuteEvent.CommandResult.denied());
            handleLogin(player, "/" + command);
        }
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        RegisteredServer target = event.getResult().getServer().orElse(null);

        if (target == null) return;

        String playerName = player.getUsername();
        String loginServerName = configManager.getLoginServerName();

        if (loggedInPlayerList.contains(playerName)) return;

        String targetName = target.getServerInfo().getName();
        if (targetName.equals(loginServerName)) {
            handleLogin(player, null);
            return;
        }

        checkLoginSync(player, playerName, loginServerName, event);
    }

    private void checkLoginSync(Player player, String playerName, String loginServerName,
                                  ServerPreConnectEvent event) {
        try {
            if (communication.sendConnectRequest(playerName) == 1) {
                loggedInPlayerList.add(playerName);
            } else {
                redirectToLoginServer(loginServerName, event);
            }
        } catch (Exception e) {
            logger.error("Error checking login status for player: " + playerName, e);
        }
    }

    private void redirectToLoginServer(String loginServerName, ServerPreConnectEvent event) {
        proxyServer.getServer(loginServerName)
                .ifPresent(loginServer ->
                        event.setResult(ServerPreConnectEvent.ServerResult.allowed(loginServer))
                );
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        String serverName = event.getServer().getServerInfo().getName();
        String loginServerName = configManager.getLoginServerName();
        
        if (serverName.equals(loginServerName) && loggedInPlayerList.contains(player.getUsername())) {
            PluginMain.runAsyncDelayed(() -> {
                communication.sendKeepLoggedInRequest(player.getUsername());
            }, 1, TimeUnit.SECONDS);
        }
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        if (player != null) {
            try {
                loggedInPlayerList.remove(player.getUsername());
            } catch (Exception e) {
                logger.warn("Failed to remove player from logged-in list: " + player.getUsername());
            }
        }
    }

    @Subscribe
    public void onPreLogin(PreLoginEvent event) {
        String playerName = event.getUsername();
        
        try {
            if (loggedInPlayerList.contains(playerName) && (communication.sendConnectRequest(playerName) == 1)) {
                event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                    Component.text(MessageKey.ALREADY_LOGGED_IN_PROXY.get())
                ));
            }
        } catch (Exception e) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                Component.text(MessageKey.ERROR_PLEASE_RETRY.get())
            ));
        }
    }

    private boolean isNotLoggedIn(Player player) {
        return !loggedInPlayerList.contains(player.getUsername());
    }

    private void handleLogin(Player player, String message) {
        String playerName = player.getUsername();
        PluginMain.runAsync(() -> handleLoginAsync(player, playerName, message));
    }

    private void handleLoginAsync(Player player, String playerName, String message) {
        try {
            if (communication.sendConnectRequest(playerName) != 1) return;

            loggedInPlayerList.add(playerName);
            executeQueuedCommand(player, message);
        } catch (Exception e) {
            logger.error("Error handling login for player: " + playerName, e);
        }
    }

    private void executeQueuedCommand(Player player, String message) {
        if (message == null || !message.startsWith("/")) return;
        proxyServer
            .getCommandManager()
            .executeAsync(player, message.substring(1));
    }
    
    public List<String> getLoggedInPlayers() {
        return loggedInPlayerList;
    }
}