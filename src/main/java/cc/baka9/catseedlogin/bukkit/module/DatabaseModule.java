package cc.baka9.catseedlogin.bukkit.module;

import cc.baka9.catseedlogin.bukkit.Cache;
import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.bukkit.database.MySQL;
import cc.baka9.catseedlogin.bukkit.database.SQLite;
import cc.baka9.catseedlogin.bukkit.database.SQL;
import cc.baka9.catseedlogin.bukkit.PluginContext;

/** Owns database startup, cache hydration, and connection shutdown. */
public final class DatabaseModule implements BukkitModule {
  @Override
  public void enable(CatSeedLogin plugin) throws Exception {
    CatSeedLogin.sql = plugin.getConfigManager().isMySQL() ? new MySQL(plugin) : new SQLite(plugin);
    CatSeedLogin.sql.init();
    Cache.refreshAll();
    PluginContext.init(plugin, CatSeedLogin.sql, CatSeedLogin.loadProtocolLib);
  }

  @Override
  public void disable(CatSeedLogin plugin) {
    SQL sql = CatSeedLogin.sql;
    if (sql == null) return;
    try {
      sql.closeConnection();
    } catch (Exception error) {
      plugin.getLogger().warning("关闭数据库连接时出错: " + error.getMessage());
    } finally {
      CatSeedLogin.sql = null;
    }
  }
}
