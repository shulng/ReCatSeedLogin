package cc.baka9.catseedlogin.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.Player;

import java.util.concurrent.TimeUnit;

/**
 * Velocity event listeners.
 */
public class Listeners {

    private final PluginMain plugin;

    public Listeners(PluginMain plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onPreLogin(PreLoginEvent event) {
        String name = event.getUsername();

        if (plugin.getSessionManager().isLoggedIn(name)) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                    net.kyori.adventure.text.Component.text("Already logged in")
            ));
        }
    }

    @Subscribe
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (!plugin.getSessionManager().isLoggedIn(player.getUsername())) {
            // Handle login command
            if (message.startsWith("/")) {
                event.setResult(PlayerChatEvent.ChatResult.denied());
                // TODO: Handle login command
            }
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        String name = event.getPlayer().getUsername();
        // Session cleanup handled by session manager
    }
}
