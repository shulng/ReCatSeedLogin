package cc.baka9.catseedlogin.bukkit.lifecycle;

import cc.baka9.catseedlogin.bukkit.BukkitContext;
import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.bukkit.communication.CommunicationServer;
import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.database.MySQL;
import cc.baka9.catseedlogin.bukkit.database.SQL;
import cc.baka9.catseedlogin.bukkit.database.SQLite;
import cc.baka9.catseedlogin.bukkit.listener.BlindingListeners;
import cc.baka9.catseedlogin.bukkit.listener.BukkitListeners;
import cc.baka9.catseedlogin.bukkit.task.Task;

/**
 * 插件启用流程：按固定顺序装载数据库、上下文、软依赖、监听器、指令与定时任务。
 *
 * <p>配置与调度器的初始化由主类完成（它们属于插件自身状态），本类只负责"装载动作"的编排。
 */
public final class BukkitStartup {

  private BukkitStartup() {}

  /**
   * 执行启用流程。
   *
   * @return {@code true} 表示全部就绪；{@code false} 表示数据库不可用，调用方应禁用插件。
   */
  public static boolean enable(CatSeedLogin plugin, BukkitConfigManager config) {
    SQL sql = initDatabase(plugin, config);
    if (sql == null) {
      return false;
    }

    BukkitContext.init(plugin, sql);
    Cache.refreshAll();

    registerListeners(plugin);
    BukkitHooks.detect(config);

    if (config.isProxyEnabled()) {
      CommunicationServer.startAsync();
    }

    BukkitCommands.registerAll(plugin);

    Task.runAll();
    return true;
  }

  /** 按配置选择 MySQL / SQLite 并建表，失败时返回 {@code null}。 */
  private static SQL initDatabase(CatSeedLogin plugin, BukkitConfigManager config) {
    try {
      SQL sql = config.isMySQL() ? new MySQL(plugin, config) : new SQLite(plugin);
      sql.init();
      return sql;
    } catch (Exception e) {
      plugin.getLogger().warning("§c加载数据库时出错");
      e.printStackTrace();
      return null;
    }
  }

  private static void registerListeners(CatSeedLogin plugin) {
    plugin.getServer().getPluginManager().registerEvents(new BukkitListeners(), plugin);
    plugin.getServer().getPluginManager().registerEvents(new BlindingListeners(), plugin);
  }
}
