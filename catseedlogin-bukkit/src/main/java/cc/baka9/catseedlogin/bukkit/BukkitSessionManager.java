package cc.baka9.catseedlogin.bukkit;

import cc.baka9.catseedlogin.api.CSLPlayer;
import cc.baka9.catseedlogin.api.SessionManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Bukkit implementation of SessionManager.
 * Tracks logged-in players.
 */
public class BukkitSessionManager implements SessionManager {

    private final CatSeedLogin plugin;
    private final Map<String, Long> loggedInPlayers = new ConcurrentHashMap<>();

    public BukkitSessionManager(CatSeedLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLogin(CSLPlayer player) {
        loggedInPlayers.put(player.getName().toLowerCase(), System.currentTimeMillis());
    }

    @Override
    public void onLogout(CSLPlayer player) {
        loggedInPlayers.remove(player.getName().toLowerCase());
    }

    @Override
    public boolean isLoggedIn(String name) {
        return loggedInPlayers.containsKey(name.toLowerCase());
    }

    /**
     * Get all logged-in player names.
     */
    public java.util.Set<String> getLoggedInPlayers() {
        return java.util.Collections.unmodifiableSet(loggedInPlayers.keySet());
    }
}
