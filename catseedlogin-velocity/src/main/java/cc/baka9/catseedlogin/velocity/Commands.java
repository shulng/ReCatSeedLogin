package cc.baka9.catseedlogin.velocity;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Velocity commands.
 */
public class Commands implements SimpleCommand {

    private final PluginMain plugin;

    public Commands(PluginMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

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
        }
    }

    private void handleReload(CommandSource source) {
        plugin.getConfigLoader().reload();
        source.sendMessage(Component.text("Config reloaded", NamedTextColor.GREEN));
    }

    private void handleStatus(CommandSource source) {
        String host = plugin.getConfigLoader().getString("proxy.host", "127.0.0.1");
        int port = plugin.getConfigLoader().getInt("proxy.port", 2333);
        String serverName = plugin.getConfigLoader().getString("proxy.login-server-name", "lobby");

        source.sendMessage(Component.text("CatSeedLogin Velocity Status:", NamedTextColor.GOLD));
        source.sendMessage(Component.text("Proxy Host: ", NamedTextColor.GRAY).append(Component.text(host, NamedTextColor.WHITE)));
        source.sendMessage(Component.text("Proxy Port: ", NamedTextColor.GRAY).append(Component.text(port, NamedTextColor.WHITE)));
        source.sendMessage(Component.text("Login Server: ", NamedTextColor.GRAY).append(Component.text(serverName, NamedTextColor.WHITE)));
    }

    private void handleList(CommandSource source) {
        java.util.Set<String> loggedIn = plugin.getSessionManager().getLoggedInPlayers();
        if (loggedIn.isEmpty()) {
            source.sendMessage(Component.text("No players logged in", NamedTextColor.GRAY));
        } else {
            source.sendMessage(Component.text("Logged in players:", NamedTextColor.GOLD));
            for (String name : loggedIn) {
                source.sendMessage(Component.text("- ", NamedTextColor.GRAY).append(Component.text(name, NamedTextColor.WHITE)));
            }
        }
    }

    private void sendHelp(CommandSource source) {
        source.sendMessage(Component.text("CatSeedLogin Velocity Commands:", NamedTextColor.GOLD));
        source.sendMessage(Component.text("/cslv reload - Reload config", NamedTextColor.GRAY));
        source.sendMessage(Component.text("/cslv status - Show status", NamedTextColor.GRAY));
        source.sendMessage(Component.text("/cslv list - List logged in players", NamedTextColor.GRAY));
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] currentArgs = invocation.arguments();
        if (currentArgs.length == 1) {
            return CompletableFuture.completedFuture(Arrays.asList("reload", "status", "list"));
        }
        return CompletableFuture.completedFuture(java.util.Collections.emptyList());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("catseedlogin.admin");
    }
}
