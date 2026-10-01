package cc.baka9.catseedlogin.bukkit;

import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager;
import cc.baka9.catseedlogin.bukkit.database.SQL;
import java.util.logging.Logger;

/**
 * Bukkit 端的运行时上下文（服务定位器）：持有插件实例、数据库连接与配置管理器， 避免在业务类中到处传递插件对象或访问静态字段。
 */
public final class BukkitContext {

  private static volatile BukkitContext instance;

  private final CatSeedLogin plugin;
  private volatile SQL sql;

  private BukkitContext(CatSeedLogin plugin, SQL sql) {
    this.plugin = plugin;
    this.sql = sql;
  }

  public static void init(CatSeedLogin plugin, SQL sql) {
    instance = new BukkitContext(plugin, sql);
  }

  public static BukkitContext get() {
    if (instance == null) {
      throw new IllegalStateException("BukkitContext has not been initialized");
    }
    return instance;
  }

  /** 上下文是否已就绪，用于插件启用失败时的防御性判断。 */
  public static boolean isInitialized() {
    return instance != null;
  }

  public static CatSeedLogin getPlugin() {
    return get().plugin;
  }

  public static SQL getSql() {
    return get().sql;
  }

  public static void setSql(SQL sql) {
    get().sql = sql;
  }

  public static BukkitConfigManager getConfigManager() {
    return get().plugin.getConfigManager();
  }

  public static Logger getLogger() {
    return get().plugin.getLogger();
  }

  public static boolean isLoadProtocolLib() {
    return get().plugin.isProtocolLibLoaded();
  }
}
