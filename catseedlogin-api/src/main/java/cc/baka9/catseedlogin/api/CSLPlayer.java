package cc.baka9.catseedlogin.api;

import java.util.UUID;

/**
 * Type-safe player reference.
 * Replaces the old Object return type from getPlatformPlayer().
 */
public interface CSLPlayer {

    /**
     * Get the player's name.
     */
    String getName();

    /**
     * Get the player's UUID.
     */
    UUID getUniqueId();

    /**
     * Check if the player is currently online on the server.
     */
    boolean isOnline();

    /**
     * Check if the player has a registered account.
     */
    boolean isRegistered();

    /**
     * Check if the player has successfully logged in.
     */
    boolean isLoggedIn();

    /**
     * Check if this is a Bedrock player (Floodgate bypass).
     */
    boolean isPremium();
}
