package cc.baka9.catseedlogin.api;

import java.util.Optional;
import java.util.UUID;

/**
 * Main API entry point for CatSeedLogin.
 * Provides static access to core functionality.
 */
public final class CatSeedLoginAPI {

    public static final int API_VERSION = 1;

    private static CatSeedLoginPlugin plugin;

    private CatSeedLoginAPI() {}

    /**
     * Internal: called by platform modules during initialization.
     */
    public static void init(CatSeedLoginPlugin plugin) {
        CatSeedLoginAPI.plugin = plugin;
    }

    /**
     * Get the plugin instance.
     */
    public static Optional<CatSeedLoginPlugin> getPlugin() {
        return Optional.ofNullable(plugin);
    }

    /**
     * Get a player by name.
     */
    public static CSLPlayer getPlayer(String name) {
        if (plugin == null) return null;
        return plugin.getPlayer(name);
    }

    /**
     * Get a player by UUID.
     */
    public static CSLPlayer getPlayer(UUID uuid) {
        if (plugin == null) return null;
        return plugin.getPlayer(uuid);
    }

    /**
     * Check if a player is registered.
     */
    public static boolean isRegistered(String name) {
        if (plugin == null) return false;
        CSLPlayer player = plugin.getPlayer(name);
        return player != null && player.isRegistered();
    }

    /**
     * Check if a player is logged in.
     */
    public static boolean isLoggedIn(String name) {
        if (plugin == null) return false;
        CSLPlayer player = plugin.getPlayer(name);
        return player != null && player.isLoggedIn();
    }
}
