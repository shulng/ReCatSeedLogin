package cc.baka9.catseedlogin.bukkit.platform;

import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager;
import cc.baka9.catseedlogin.bukkit.database.SQL;
import java.util.logging.Logger;

public class BukkitContext {
  private static volatile BukkitContext instance;

  private final CatSeedLogin plugin;
  private SQL sql;
  private boolean loadProtocolLib;

  private BukkitContext(CatSeedLogin plugin, SQL sql, boolean loadProtocolLib) {
    this.plugin = plugin;
    this.sql = sql;
    this.loadProtocolLib = loadProtocolLib;
  }

  public static void init(CatSeedLogin plugin, SQL sql, boolean loadProtocolLib) {
    instance = new BukkitContext(plugin, sql, loadProtocolLib);
  }

  public static BukkitContext get() {
    if (instance == null) {
      throw new IllegalStateException("BukkitContext has not been initialized");
    }
    return instance;
  }

  public static SQL getSql() {
    return get().sql;
  }

  public static void setSql(SQL sql) {
    get().sql = sql;
    get().plugin.sql = sql;
  }

  public static boolean isLoadProtocolLib() {
    return get().loadProtocolLib;
  }

  public static Logger getLogger() {
    return get().plugin.getLogger();
  }

  public static BukkitConfigManager getConfigManager() {
    return get().plugin.getConfigManager();
  }

  public static CatSeedLogin getPlugin() {
    return get().plugin;
  }
}
