package cc.baka9.catseedlogin.bukkit.database;

import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL extends SQL {

  private final CatSeedLogin plugin;

  public MySQL(CatSeedLogin plugin) {
    super(plugin.getLogger());
    this.plugin = plugin;
  }

  @Override
  protected Connection openConnection() throws SQLException {
    BukkitConfigManager cm = plugin.getConfigManager();
    String url =
        "jdbc:mysql://"
            + cm.getDatabaseHost()
            + ":"
            + cm.getDatabasePort()
            + "/"
            + cm.getDatabaseName()
            + "?characterEncoding=UTF-8";
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      return DriverManager.getConnection(url, cm.getDatabaseUser(), cm.getDatabasePassword());
    } catch (ClassNotFoundException | SQLException e) {
      throw new SQLException(e);
    }
  }
}
