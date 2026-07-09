package cc.baka9.catseedlogin.velocity;

import cc.baka9.catseedlogin.api.CSLPlayer;
import cc.baka9.catseedlogin.api.SessionManager;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Velocity session manager.
 */
public class VelocitySessionManager implements SessionManager {

    private final Set<String> loggedInPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public void onLogin(CSLPlayer player) {
        loggedInPlayers.add(player.getName().toLowerCase());
    }

    @Override
    public void onLogout(CSLPlayer player) {
        loggedInPlayers.remove(player.getName().toLowerCase());
    }

    @Override
    public boolean isLoggedIn(String name) {
        return loggedInPlayers.contains(name.toLowerCase());
    }

    public Set<String> getLoggedInPlayers() {
        return Collections.unmodifiableSet(loggedInPlayers);
    }
}
