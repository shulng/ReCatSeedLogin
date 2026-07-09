package cc.baka9.catseedlogin.api;

/**
 * Fired when a player logs out (disconnects).
 * Cannot be cancelled.
 */
public class PlayerLogoutEvent extends CatSeedEvent {

    private final CSLPlayer player;

    public PlayerLogoutEvent(CSLPlayer player) {
        this.player = player;
    }

    public CSLPlayer getPlayer() {
        return player;
    }
}
