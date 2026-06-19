package cc.baka9.catseedlogin.velocity;

import cc.baka9.catseedlogin.common.i18n.MessageKey;
import cc.baka9.catseedlogin.velocity.config.VelocityConfigManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Commands implements SimpleCommand {

    private final VelocityConfigManager configManager;
    private final ProxyServer proxyServer;
    private final Logger logger;

    public Commands(VelocityConfigManager configManager, ProxyServer proxyServer, Logger logger) {
        this.configManager = configManager;
        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        
        try {
            if (!source.hasPermission("catseedlogin.admin")) {
                source.sendMessage(Component.text(MessageKey.NO_PERMISSION.get()));
                return;
            }
            
            if (args.length == 0) {
                sendHelp(source);
                return;
            }
            
            switch (args[0].toLowerCase()) {
                case "reload":
                    handleReload(source);
                    break;
                case "status":
                    handleStatus(source);
                    break;
                case "list":
                    handleList(source);
                    break;
                default:
                    sendHelp(source);
                    break;
            }
        } catch (Exception e) {
            source.sendMessage(Component.text(MessageKey.INTERNAL_ERROR.get(), NamedTextColor.RED));
            logger.error("Error executing command", e);
        }
    }
    
    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] args = invocation.arguments();
        
        if (args.length <= 1) {
            return CompletableFuture.completedFuture(Arrays.asList("reload", "status", "list"));
        }
        
        return CompletableFuture.completedFuture(new ArrayList<>());
    }
    
    private void sendHelp(CommandSource source) {
        source.sendMessage(Component.text("=== CatSeedLogin-Velocity ===", NamedTextColor.GOLD));
        source.sendMessage(Component.text("/cslv reload", NamedTextColor.YELLOW));
        source.sendMessage(Component.text("/cslv status", NamedTextColor.YELLOW));
        source.sendMessage(Component.text("/cslv list", NamedTextColor.YELLOW));
    }
    
    private void handleReload(CommandSource source) {
        try {
            configManager.reload();
            source.sendMessage(Component.text(MessageKey.CONFIG_RELOADED.get()));
        } catch (Exception e) {
            source.sendMessage(Component.text(MessageKey.INTERNAL_ERROR.get(), NamedTextColor.RED));
            logger.error("Failed to reload config", e);
        }
    }
    
    private void handleStatus(CommandSource source) {
        try {
            source.sendMessage(Component.text("=== CatSeedLogin-Velocity ===", NamedTextColor.GOLD));
            
            String host = configManager.getProxyHost();
            int port = configManager.getProxyPort();
            String loginServerName = configManager.getLoginServerName();
            
            source.sendMessage(Component.text(host + ":" + port, NamedTextColor.YELLOW));
            source.sendMessage(Component.text(loginServerName, NamedTextColor.YELLOW));
            
            boolean loginServerOnline = proxyServer
                .getServer(loginServerName)
                .isPresent();
            
            source.sendMessage(Component.text(loginServerOnline ? "Online" : "Offline", 
                loginServerOnline ? NamedTextColor.GREEN : NamedTextColor.RED));
        } catch (Exception e) {
            source.sendMessage(Component.text(MessageKey.INTERNAL_ERROR.get(), NamedTextColor.RED));
            logger.error("Error getting status", e);
        }
    }
    
    private void handleList(CommandSource source) {
        try {
            Listeners listeners = PluginMain.getInstance().getListeners();
            List<String> loggedInPlayers = listeners.getLoggedInPlayers();
            
            source.sendMessage(Component.text("=== " + loggedInPlayers.size() + " ===", NamedTextColor.GOLD));
            
            displayPlayerList(source, loggedInPlayers);
        } catch (Exception e) {
            source.sendMessage(Component.text(MessageKey.INTERNAL_ERROR.get(), NamedTextColor.RED));
            logger.error("Error getting player list", e);
        }
    }

    private void displayPlayerList(CommandSource source, List<String> loggedInPlayers) {
        if (loggedInPlayers.isEmpty()) {
            source.sendMessage(Component.text("None", NamedTextColor.GRAY));
            return;
        }
        loggedInPlayers.forEach(playerName ->
            source.sendMessage(Component.text("- " + playerName, NamedTextColor.WHITE))
        );
    }
}