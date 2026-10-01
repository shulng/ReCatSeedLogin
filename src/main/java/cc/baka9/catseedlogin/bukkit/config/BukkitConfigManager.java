package cc.baka9.catseedlogin.bukkit.config;

import cc.baka9.catseedlogin.bukkit.util.LocationUtil;
import cc.baka9.catseedlogin.bukkit.util.WorldUtil;
import cc.baka9.catseedlogin.common.api.PlatformAdapter;
import cc.baka9.catseedlogin.common.config.ConfigConstants;
import cc.baka9.catseedlogin.common.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/** Bukkit 端配置管理器，在通用配置之上补充出生点 {@link Location} 的读写能力。 */
public class BukkitConfigManager extends ConfigManager {

  public BukkitConfigManager(PlatformAdapter platform) {
    super(platform);
  }

  /** 把指定位置持久化到 {@code spawn.location}。 */
  public void setSpawnLocation(Location location) {
    if (location.getWorld() == null) return;
    set(ConfigConstants.Path.SPAWN_LOCATION, LocationUtil.format(location));
  }

  /**
   * 解析配置的出生点；未配置或配置非法时回退到默认世界出生点。
   *
   * <p>该方法只做读取，不会回写配置文件。
   */
  public Location getBukkitSpawnLocation() {
    String locStr = mainConfig.getString(ConfigConstants.Path.SPAWN_LOCATION);
    if (locStr == null || locStr.isEmpty()) {
      return defaultSpawnLocation();
    }
    String[] parts = locStr.split(":");
    if (parts.length < 6) {
      return defaultSpawnLocation();
    }
    World world = Bukkit.getWorld(parts[0]);
    if (world == null) {
      world = WorldUtil.getDefaultWorld();
    }
    if (world == null) {
      return defaultSpawnLocation();
    }
    try {
      return new Location(
          world,
          Double.parseDouble(parts[1]),
          Double.parseDouble(parts[2]),
          Double.parseDouble(parts[3]),
          Float.parseFloat(parts[4]),
          Float.parseFloat(parts[5]));
    } catch (NumberFormatException e) {
      return world.getSpawnLocation();
    }
  }

  private Location defaultSpawnLocation() {
    World world = WorldUtil.getDefaultWorld();
    if (world != null) {
      return world.getSpawnLocation();
    }
    return Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0).getSpawnLocation();
  }
}
