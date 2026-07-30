package cc.baka9.catseedlogin.bukkit.config;

import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.bukkit.util.WorldUtil;
import cc.baka9.catseedlogin.common.config.BaseConfigManager;
import cc.baka9.catseedlogin.common.config.ConfigConstants;
import java.io.InputStream;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

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
    String locStr =
        location.getWorld().getName()
            + ":"
            + location.getX()
            + ":"
            + location.getY()
            + ":"
            + location.getZ()
            + ":"
            + location.getYaw()
            + ":"
            + location.getPitch();
    mainConfig.set(ConfigConstants.Path.SPAWN_LOCATION, locStr);
    saveConfig("config.yml");
  }

  public Location getBukkitSpawnLocation() {
    String locStr = mainConfig.getString(ConfigConstants.Path.SPAWN_LOCATION);
    World defaultWorld = WorldUtil.getDefaultWorld(plugin.getLogger());

    if (locStr == null || locStr.isEmpty()) {
      Location spawn;
      if (defaultWorld != null) {
        spawn = defaultWorld.getSpawnLocation();
      } else if (!Bukkit.getWorlds().isEmpty()) {
        spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
      } else {
        return null;
      }
      setSpawnLocation(spawn);
      return spawn;
    }

    String[] parts = locStr.split(":");
    if (parts.length < 6) {
      Location spawn;
      if (defaultWorld != null) {
        spawn = defaultWorld.getSpawnLocation();
      } else if (!Bukkit.getWorlds().isEmpty()) {
        spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
      } else {
        return null;
      }
      setSpawnLocation(spawn);
      return spawn;
    }

    World world = Bukkit.getWorld(parts[0]);
    if (world == null) {
      world = defaultWorld;
    }
    if (world == null) {
      if (!Bukkit.getWorlds().isEmpty()) {
        world = Bukkit.getWorlds().get(0);
      } else {
        return null;
      }
    }

    try {
      double x = Double.parseDouble(parts[1]);
      double y = Double.parseDouble(parts[2]);
      double z = Double.parseDouble(parts[3]);
      float yaw = Float.parseFloat(parts[4]);
      float pitch = Float.parseFloat(parts[5]);
      return new Location(world, x, y, z, yaw, pitch);
    } catch (NumberFormatException e) {
      Location spawn = world.getSpawnLocation();
      setSpawnLocation(spawn);
      return spawn;
    }
  }
}
