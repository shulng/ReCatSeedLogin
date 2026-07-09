package cc.baka9.catseedlogin.bungee;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

/**
 * BungeeCord admin commands.
 */
public class BungeeCommands extends Command {

    private final PluginMain plugin;

    public BungeeCommands(PluginMain plugin) {
        super("cslb", "catseedlogin.admin", "csb");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
            case "status":
                handleStatus(sender);
                break;
            case "list":
                handleList(sender);
                break;
            default:
                sendHelp(sender);
        }
    }

    private void handleReload(CommandSender sender) {
        plugin.getConfigLoader().reload();
        sender.sendMessage(new TextComponent("§aConfig reloaded"));
    }

    private void handleStatus(CommandSender sender) {
        String host = plugin.getConfigLoader().getString("proxy.host", "127.0.0.1");
        int port = plugin.getConfigLoader().getInt("proxy.port", 2333);
        String serverName = plugin.getConfigLoader().getString("proxy.login-server-name", "lobby");

        sender.sendMessage(new TextComponent("§6CatSeedLogin BungeeCord Status:"));
        sender.sendMessage(new TextComponent("§7Proxy Host: §f" + host));
        sender.sendMessage(new TextComponent("§7Proxy Port: §f" + port));
        sender.sendMessage(new TextComponent("§7Login Server: §f" + serverName));
    }

    private void handleList(CommandSender sender) {
        java.util.Set<String> loggedIn = plugin.getSessionManager().getLoggedInPlayers();
        if (loggedIn.isEmpty()) {
            sender.sendMessage(new TextComponent("§7No players logged in"));
        } else {
            sender.sendMessage(new TextComponent("§6Logged in players:"));
            for (String name : loggedIn) {
                sender.sendMessage(new TextComponent("§7- §f" + name));
            }
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(new TextComponent("§6CatSeedLogin BungeeCord Commands:"));
        sender.sendMessage(new TextComponent("§7/cslb reload - Reload config"));
        sender.sendMessage(new TextComponent("§7/cslb status - Show status"));
        sender.sendMessage(new TextComponent("§7/cslb list - List logged in players"));
    }
}
