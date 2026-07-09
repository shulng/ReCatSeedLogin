package cc.baka9.catseedlogin.bungee;

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * BungeeCord event listeners.
 */
public class Listeners implements Listener {

    private final PluginMain plugin;

    public Listeners(PluginMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPreLogin(PreLoginEvent event) {
        String name = event.getConnection().getName();

        // Check if player is already connected
        if (plugin.getSessionManager().isLoggedIn(name)) {
            event.setCancelReason("Already logged in");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        String name = event.getPlayer().getName();

        // Check if player is logged in before allowing server connection
        if (!plugin.getSessionManager().isLoggedIn(name)) {
            // TODO: Redirect to login server
        }
    }

    @EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
        String name = event.getPlayer().getName();

        // Send keep-logged-in request to Bukkit server
        if (!plugin.getSessionManager().isLoggedIn(name)) {
            plugin.getPlatformAdapter().runAsync(() -> {
                // TODO: Send keep-logged-in request
            });
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        String name = event.getPlayer().getName();
        // Session cleanup handled by session manager
    }
}
