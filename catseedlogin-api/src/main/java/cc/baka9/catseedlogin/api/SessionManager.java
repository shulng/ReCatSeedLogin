package cc.baka9.catseedlogin.api;

/**
 * Session manager for player authentication.
 * Allows other plugins to manage login sessions.
 */
public interface SessionManager {

    /**
     * Called when a player logs in successfully.
     */
    void onLogin(CSLPlayer player);

    /**
     * Called when a player logs out.
     */
    void onLogout(CSLPlayer player);

    /**
     * Check if a player is currently logged in.
     */
    boolean isLoggedIn(String name);
}
