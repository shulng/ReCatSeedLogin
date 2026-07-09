package cc.baka9.catseedlogin.api;

/**
 * Hook for custom validation before login/register.
 * Return null to allow, or a rejection message to deny.
 */
public interface PreLoginHook {

    /**
     * Validate a player before login attempt.
     *
     * @param player the player attempting login (may be null for unregistered)
     * @param ip the player's IP address
     * @param password the plaintext password
     * @return null to allow, or a message to reject
     */
    String validate(CSLPlayer player, String ip, String password);
}
