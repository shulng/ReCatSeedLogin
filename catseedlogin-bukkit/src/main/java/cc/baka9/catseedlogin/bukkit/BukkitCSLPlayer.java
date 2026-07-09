package cc.baka9.catseedlogin.bukkit;

import cc.baka9.catseedlogin.api.CSLPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Bukkit implementation of CSLPlayer.
 * Wraps Bukkit's Player object.
 */
public class BukkitCSLPlayer implements CSLPlayer {

    private final CatSeedLogin plugin;
    private final String name;

    public BukkitCSLPlayer(CatSeedLogin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getUniqueId() {
        Player player = Bukkit.getPlayerExact(name);
        return player != null ? player.getUniqueId() : null;
    }

    @Override
    public boolean isOnline() {
        Player player = Bukkit.getPlayerExact(name);
        return player != null && player.isOnline();
    }

    @Override
    public boolean isRegistered() {
        return plugin.getCache().isRegistered(name);
    }

    @Override
    public boolean isLoggedIn() {
        // TODO: Check login state via session manager
        return false;
    }

    @Override
    public boolean isPremium() {
        // TODO: Check Floodgate
        return false;
    }
}
