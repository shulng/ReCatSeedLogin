package cc.baka9.catseedlogin.bukkit;

import cc.baka9.catseedlogin.common.model.LoginPlayerData;
import cc.baka9.catseedlogin.common.crypto.HashVersion;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * In-memory player cache.
 * Thread-safe, loads all accounts at startup.
 */
public class Cache {

    private final CatSeedLogin plugin;
    private final Map<String, LoginPlayerData> cache = new ConcurrentHashMap<>();
    private final AtomicBoolean loaded = new AtomicBoolean(false);

    public Cache(CatSeedLogin plugin) {
        this.plugin = plugin;
    }

    /**
     * Load all accounts from database (async).
     */
    public void refreshAll() {
        loaded.set(false);
        plugin.getPlatformAdapter().runAsync(() -> {
            try {
                // TODO: Load from database
                // List<LoginPlayerData> accounts = database.getAllAccounts();
                // cache.clear();
                // for (LoginPlayerData account : accounts) {
                //     cache.put(account.getName().toLowerCase(), account);
                // }
                loaded.set(true);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load accounts: " + e.getMessage());
            }
        });
    }

    /**
     * Get a player by name (case-insensitive).
     */
    public LoginPlayerData get(String name) {
        return cache.get(name.toLowerCase());
    }

    /**
     * Check if cache is loaded.
     */
    public boolean isLoaded() {
        return loaded.get();
    }

    /**
     * Get all cached players.
     */
    public Collection<LoginPlayerData> getAll() {
        return Collections.unmodifiableCollection(cache.values());
    }

    /**
     * Update a single player in cache.
     */
    public void put(LoginPlayerData data) {
        cache.put(data.getName().toLowerCase(), data);
    }

    /**
     * Remove a player from cache.
     */
    public void remove(String name) {
        cache.remove(name.toLowerCase());
    }

    /**
     * Check if a player is registered.
     */
    public boolean isRegistered(String name) {
        return cache.containsKey(name.toLowerCase());
    }
}
