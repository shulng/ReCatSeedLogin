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
    if (location == null || location.getWorld() == null) return;
    set(ConfigConstants.Path.SPAWN_LOCATION, LocationUtil.format(location));
  }

  /** 插件启用时调用：确保 {@code spawn.location} 已被填充为实际生效的出生点。 */
  public void ensureSpawnLocationPersisted() {
    getBukkitSpawnLocation();
  }

  /**
   * 解析配置的出生点。
   *
   * <p>{@code spawn.location} 为空、格式不完整或坐标非法时，取世界默认出生点并<b>回写</b>到配置文件，
   * 使配置始终反映实际生效的值。仅当服务器尚未加载任何世界时才返回 {@code null}。
   */
  public Location getBukkitSpawnLocation() {
    String locStr = mainConfig.getString(ConfigConstants.Path.SPAWN_LOCATION);
    if (locStr == null || locStr.isEmpty()) {
      return persistDefaultSpawn(null);
    }

    String[] parts = locStr.split(":");
    if (parts.length < 6) {
      return persistDefaultSpawn(null);
    }

    World configuredWorld = Bukkit.getWorld(parts[0]);
    World resolvedWorld = resolveWorld(configuredWorld);
    if (resolvedWorld == null) {
      return null;
    }
    try {
      Location location =
          new Location(
              resolvedWorld,
              Double.parseDouble(parts[1]),
              Double.parseDouble(parts[2]),
              Double.parseDouble(parts[3]),
              Float.parseFloat(parts[4]),
              Float.parseFloat(parts[5]));
      // 世界名与实际不符（例如世界被删除后回退），同步修正配置
      if (configuredWorld == null) {
        setSpawnLocation(location);
      }
      return location;
    } catch (NumberFormatException e) {
      return persistDefaultSpawn(resolvedWorld);
    }
  }

  /** 解析失败时采用世界出生点，并将其写回 {@code spawn.location}。 */
  private Location persistDefaultSpawn(World preferredWorld) {
    Location spawn = defaultSpawnLocation(preferredWorld);
    setSpawnLocation(spawn);
    return spawn;
  }

  private Location defaultSpawnLocation(World preferredWorld) {
    World world = resolveWorld(preferredWorld);
    return world == null ? null : world.getSpawnLocation();
  }

  /** 优先使用指定世界，其次 {@code server.properties} 的 level-name 世界，最后回退到第一个世界。 */
  private World resolveWorld(World preferredWorld) {
    if (preferredWorld != null) {
      return preferredWorld;
    }
    World world = WorldUtil.getDefaultWorld();
    if (world != null) {
      return world;
    }
    return Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
  }
}
