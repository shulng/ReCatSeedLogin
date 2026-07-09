package cc.baka9.catseedlogin.api;

/**
 * Fired when a player attempts to login.
 * Cancel to prevent login.
 */
public class PlayerLoginEvent extends CatSeedEvent {

    public enum LoginResult {
        SUCCESS,
        FAIL,
        ALREADY_LOGGED_IN
    }

    private final CSLPlayer player;
    private final LoginResult result;

    public PlayerLoginEvent(CSLPlayer player, LoginResult result) {
        this.player = player;
        this.result = result;
    }

    public CSLPlayer getPlayer() {
        return player;
    }

    public LoginResult getResult() {
        return result;
    }
}
