package cc.baka9.catseedlogin.bukkit.database;

import cc.baka9.catseedlogin.bukkit.config.Config;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.bukkit.plugin.java.JavaPlugin;

public class MySQL extends SQL {

  public MySQL(JavaPlugin javaPlugin) {
    super(javaPlugin.getLogger());
  }

  @Override
  protected Connection createConnection() throws SQLException {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      return DriverManager.getConnection(
          "jdbc:mysql://"
              + Config.MySQL.host
              + ":"
              + Config.MySQL.port
              + "/"
              + Config.MySQL.database
              + "?characterEncoding=UTF-8",
          Config.MySQL.user,
          Config.MySQL.password);
    } catch (ClassNotFoundException | SQLException e) {
      throw new SQLException(e);
    }
  }
}