package cc.baka9.catseedlogin.api;

/**
 * Fired before a player is allowed to login.
 * Cancel to reject the player with a message.
 */
public class PlayerPreLoginEvent extends CatSeedEvent {

    private final CSLPlayer player;
    private final String ip;
    private String cancelMessage;

    public PlayerPreLoginEvent(CSLPlayer player, String ip) {
        this.player = player;
        this.ip = ip;
    }

    public CSLPlayer getPlayer() {
        return player;
    }

    /**
     * Get the player's IP address.
     */
    public String getIp() {
        return ip;
    }

    /**
     * Get the message shown to the player if cancelled.
     */
    public String getCancelMessage() {
        return cancelMessage;
    }

    /**
     * Set the message shown to the player if cancelled.
     */
    public void setCancelMessage(String cancelMessage) {
        this.cancelMessage = cancelMessage;
    }
}
