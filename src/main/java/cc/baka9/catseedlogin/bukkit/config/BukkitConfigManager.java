package cc.baka9.catseedlogin.bukkit.config;

import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.common.config.BaseConfigManager;
import cc.baka9.catseedlogin.common.config.ConfigConstants;
import cc.baka9.catseedlogin.common.api.CoreConfig;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.InputStream;

public class BukkitConfigManager extends BaseConfigManager {

    private final CatSeedLogin plugin;

    public BukkitConfigManager(CatSeedLogin plugin) {
        super();
        this.plugin = plugin;
        initConfig(plugin.getDataFolder(), "config.yml");
    }

    @Override
    public InputStream getResource(String name) {
        return plugin.getResource(name);
    }

    public void setSpawnLocation(Location location) {
        if (location.getWorld() == null) return;
        String locStr = String.format("%s:%.2f:%.2f:%.2f:%.2f:%.2f",
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch());
        mainConfig.set(ConfigConstants.Path.SPAWN_LOCATION, locStr);
        saveConfig("config.yml");
    }

    public Location getBukkitSpawnLocation() {
        CoreConfig.SpawnLocation spawn = getSpawnLocation();
        World world = Bukkit.getWorld(spawn.getWorld());
        if (world == null) {
            world = Bukkit.getWorlds().get(0);
        }
        return new Location(world, spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getYaw(), spawn.getPitch());
    }

    public File getDataFolder() {
        return dataFolder;
    }
}
