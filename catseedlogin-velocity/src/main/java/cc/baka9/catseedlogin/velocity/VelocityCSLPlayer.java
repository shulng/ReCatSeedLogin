package cc.baka9.catseedlogin.velocity;

import cc.baka9.catseedlogin.api.CSLPlayer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.Optional;
import java.util.UUID;

/**
 * Velocity implementation of CSLPlayer.
 */
public class VelocityCSLPlayer implements CSLPlayer {

    private final String name;

    public VelocityCSLPlayer(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getUniqueId() {
        Optional<Player> player = getPlayer();
        return player.map(Player::getUniqueId).orElse(null);
    }

    @Override
    public boolean isOnline() {
        return getPlayer().isPresent();
    }

    @Override
    public boolean isRegistered() {
        return false;
    }

    @Override
    public boolean isLoggedIn() {
        return PluginMain.getInstance().getSessionManager().isLoggedIn(name);
    }

    @Override
    public boolean isPremium() {
        return false;
    }

    private Optional<Player> getPlayer() {
        return PluginMain.getInstance().getProxyServer().getPlayer(name);
    }
}
