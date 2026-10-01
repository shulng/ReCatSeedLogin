package cc.baka9.catseedlogin.bukkit.database;

import cc.baka9.catseedlogin.bukkit.BukkitContext;
import cc.baka9.catseedlogin.bukkit.scheduler.CatScheduler;
import cc.baka9.catseedlogin.bukkit.util.LocationUtil;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/** 玩家离线位置的读写仓储，位置以字符串形式存放在 accounts 表中。 */
public final class OfflineLocationStore {

  private OfflineLocationStore() {}

  public static Optional<Location> get(Player player) {
    try {
      return LocationUtil.parse(BukkitContext.getSql().getLocation(player.getName()));
    } catch (Exception e) {
      BukkitContext.getLogger().warning("获取玩家离线位置失败: " + player.getName());
      e.printStackTrace();
      return Optional.empty();
    }
  }

  public static void saveAsync(Player player) {
    String location = LocationUtil.format(player.getLocation());
    String name = player.getName();
    CatScheduler.runTaskAsync(() -> save(name, location));
  }

  /** 同步保存，用于插件禁用等无法异步调度的场景。 */
  public static void saveSync(Player player) {
    save(player.getName(), LocationUtil.format(player.getLocation()));
  }

  private static void save(String name, String location) {
    try {
      BukkitContext.getSql().updateLocation(name, location);
    } catch (Exception e) {
      BukkitContext.getLogger().warning("保存玩家离线位置失败: " + name);
      e.printStackTrace();
    }
  }
}
