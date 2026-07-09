package cc.baka9.catseedlogin.api;

import java.util.UUID;

/**
 * Platform-agnostic plugin interface.
 * Each platform module implements this.
 */
public interface CatSeedLoginPlugin {

    /**
     * Get the plugin version.
     */
    String getVersion();

    /**
     * Get a player by name (case-insensitive).
     */
    CSLPlayer getPlayer(String name);

    /**
     * Get a player by UUID.
     */
    CSLPlayer getPlayer(UUID uuid);

    /**
     * Get the event bus for this plugin instance.
     */
    CatSeedEventBus getEventBus();

    /**
     * Get the hook registry.
     */
    HookRegistry getHookRegistry();
}
