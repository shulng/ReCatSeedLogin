package cc.baka9.catseedlogin.bukkit.database;

import cc.baka9.catseedlogin.bukkit.BukkitContext;
import cc.baka9.catseedlogin.bukkit.scheduler.CatScheduler;
import cc.baka9.catseedlogin.common.model.LoginPlayer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 账号数据缓存：把 accounts 表全量载入内存，避免频繁访问数据库。 */
public final class Cache {

  private static volatile Map<String, LoginPlayer> playerTable = new ConcurrentHashMap<>();
  public static volatile boolean isLoaded = false;

  private Cache() {}

  public static List<LoginPlayer> getAllLoginPlayer() {
    return new ArrayList<>(playerTable.values());
  }

  public static LoginPlayer getIgnoreCase(String name) {
    return name == null ? null : playerTable.get(name.toLowerCase());
  }

  /** 异步全量刷新。 */
  public static void refreshAll() {
    CatScheduler.runTaskAsync(Cache::refreshAllSync);
  }

  /** 同步全量刷新，用于插件重载等需要立即生效的场景。 */
  public static void refreshAllSync() {
    loadAll();
  }

  /** 异步刷新单个玩家。 */
  public static void refresh(String name) {
    CatScheduler.runTaskAsync(() -> refreshOne(name));
  }

  private static void loadAll() {
    try {
      ConcurrentHashMap<String, LoginPlayer> newMap = new ConcurrentHashMap<>();
      BukkitContext.getSql().getAll().forEach(p -> newMap.put(p.getName().toLowerCase(), p));
      playerTable = newMap;
      isLoaded = true;
      BukkitContext.getLogger().info("缓存加载 " + playerTable.size() + " 个数据");
    } catch (Exception e) {
      BukkitContext.getLogger().warning("数据库错误,无法更新缓存!");
      e.printStackTrace();
    }
  }

  private static void refreshOne(String name) {
    try {
      LoginPlayer updated = BukkitContext.getSql().get(name);
      String key = name.toLowerCase();
      if (updated != null) {
        playerTable.put(key, updated);
      } else {
        playerTable.remove(key);
      }
    } catch (Exception e) {
      BukkitContext.getLogger().warning("数据库错误,无法更新缓存!");
      e.printStackTrace();
    }
  }
}
