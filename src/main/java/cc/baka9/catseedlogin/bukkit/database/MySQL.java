package cc.baka9.catseedlogin.bukkit.database;

import cc.baka9.catseedlogin.common.api.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.bukkit.plugin.java.JavaPlugin;

/** MySQL 实现，连接参数来自 {@link DatabaseConfig}。 */
public class MySQL extends SQL {

  private final DatabaseConfig config;
  private Connection connection;

  public MySQL(JavaPlugin javaPlugin, DatabaseConfig config) {
    super(javaPlugin.getLogger());
    this.config = config;
  }

  @Override
  public synchronized Connection getConnection() throws SQLException {
    if (isConnectionValid()) {
      return this.connection;
    }
    closeConnection();
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      this.connection =
          DriverManager.getConnection(
              "jdbc:mysql://"
                  + config.getDatabaseHost()
                  + ":"
                  + config.getDatabasePort()
                  + "/"
                  + config.getDatabaseName()
                  + "?characterEncoding=UTF-8",
              config.getDatabaseUser(),
              config.getDatabasePassword());
      return this.connection;
    } catch (ClassNotFoundException | SQLException e) {
      throw new SQLException(e);
    }
  }

  private boolean isConnectionValid() throws SQLException {
    if (this.connection == null || this.connection.isClosed()) {
      return false;
    }
    try (java.sql.PreparedStatement ps = this.connection.prepareStatement("SELECT 1")) {
      ps.executeQuery();
      return true;
    } catch (SQLException e) {
      return false;
    }
  }

  @Override
  public synchronized void closeConnection() {
    try {
      if (this.connection != null && !this.connection.isClosed()) {
        this.connection.close();
      }
    } catch (SQLException e) {
      logger.warning("关闭MySQL连接时出错: " + e.getMessage());
    }
    this.connection = null;
  }
}
