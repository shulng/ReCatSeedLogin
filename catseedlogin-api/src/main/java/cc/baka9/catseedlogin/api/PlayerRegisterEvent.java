package cc.baka9.catseedlogin.api;

/**
 * Fired when a player registers a new account.
 * Cancel to prevent registration.
 */
public class PlayerRegisterEvent extends CatSeedEvent {

    private final CSLPlayer player;
    private final String passwordHash;

    public PlayerRegisterEvent(CSLPlayer player, String passwordHash) {
        this.player = player;
        this.passwordHash = passwordHash;
    }

    public CSLPlayer getPlayer() {
        return player;
    }

    /**
     * Get the password hash that will be stored.
     * This is the already-hashed value, not the plaintext.
     */
    public String getPasswordHash() {
        return passwordHash;
    }
}
