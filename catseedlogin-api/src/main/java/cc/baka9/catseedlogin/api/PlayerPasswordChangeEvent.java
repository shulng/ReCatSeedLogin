package cc.baka9.catseedlogin.api;

/**
 * Fired when a player changes their password.
 * Cancel to prevent the change.
 */
public class PlayerPasswordChangeEvent extends CatSeedEvent {

    private final CSLPlayer player;

    public PlayerPasswordChangeEvent(CSLPlayer player) {
        this.player = player;
    }

    public CSLPlayer getPlayer() {
        return player;
    }
}
