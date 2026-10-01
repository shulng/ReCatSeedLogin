package cc.baka9.catseedlogin.bukkit.util;

import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/** 位置序列化工具：在 {@code 世界名:x:y:z:yaw:pitch} 字符串与 {@link Location} 之间转换。 */
public final class LocationUtil {

  private LocationUtil() {}

  public static String format(Location location) {
    return String.format(
        "%s:%.2f:%.2f:%.2f:%.2f:%.2f",
        location.getWorld().getName(),
        location.getX(),
        location.getY(),
        location.getZ(),
        location.getYaw(),
        location.getPitch());
  }

  /** 解析位置字符串，非法或世界缺失时返回 {@link Optional#empty()}。 */
  public static Optional<Location> parse(String locationString) {
    if (locationString == null || locationString.isEmpty()) {
      return Optional.empty();
    }
    String[] parts = locationString.split(":");
    if (parts.length < 6) {
      return Optional.empty();
    }
    World world = Bukkit.getWorld(parts[0]);
    if (world == null) {
      world = WorldUtil.getDefaultWorld();
    }
    if (world == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(
          new Location(
              world,
              Double.parseDouble(parts[1]),
              Double.parseDouble(parts[2]),
              Double.parseDouble(parts[3]),
              Float.parseFloat(parts[4]),
              Float.parseFloat(parts[5])));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }
}
