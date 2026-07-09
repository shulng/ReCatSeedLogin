package cc.baka9.catseedlogin.common.api;

import cc.baka9.catseedlogin.common.i18n.I18n;

/**
 * Platform abstraction interface.
 * Each platform module implements this.
 */
public interface PlatformAdapter {

    /**
     * Get the platform name (e.g., "Bukkit", "BungeeCord", "Velocity").
     */
    String getName();

    /**
     * Get the plugin version.
     */
    String getVersion();

    /**
     * Logging methods.
     */
    void logInfo(String message);
    void logWarn(String message);
    void logError(String message);
    void logError(String message, Throwable throwable);

    /**
     * Scheduler methods.
     */
    void runAsync(Runnable task);
    void runSync(Runnable task);
    void runAsyncLater(Runnable task, long delayTicks);
    void runSyncLater(Runnable task, long delayTicks);
    void runAsyncTimer(Runnable task, long delayTicks, long periodTicks);
    void runSyncTimer(Runnable task, long delayTicks, long periodTicks);

    /**
     * Get the I18n instance for this platform.
     */
    I18n getI18n();

    /**
     * Player operations.
     * Returns the platform-specific player object, or null if not found.
     */
    Object getPlatformPlayer(String name);

    /**
     * Check if a player is online.
     */
    boolean isPlayerOnline(String name);

    /**
     * Kick a player with a reason.
     */
    void kickPlayer(String name, String reason);

    /**
     * Send a message to a player.
     */
    void sendMessage(String playerName, String message);

    /**
     * Broadcast a message to all players.
     */
    void broadcast(String message);
}
