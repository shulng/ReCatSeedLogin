package cc.baka9.catseedlogin.bungee;

import cc.baka9.catseedlogin.api.CSLPlayer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

/**
 * BungeeCord implementation of CSLPlayer.
 */
public class BungeeCSLPlayer implements CSLPlayer {

    private final String name;

    public BungeeCSLPlayer(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getUniqueId() {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(name);
        return player != null ? player.getUniqueId() : null;
    }

    @Override
    public boolean isOnline() {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(name);
        return player != null && player.isConnected();
    }

    @Override
    public boolean isRegistered() {
        // BungeeCord doesn't have database access
        return false;
    }

    @Override
    public boolean isLoggedIn() {
        return PluginMain.getInstance().getSessionManager().isLoggedIn(name);
    }

    @Override
    public boolean isPremium() {
        return false;
    }
}
