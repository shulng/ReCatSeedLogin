package cc.baka9.catseedlogin.bukkit.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.bukkit.plugin.java.JavaPlugin;

public class SQLite extends SQL {
  private final JavaPlugin plugin;

  public SQLite(JavaPlugin javaPlugin) {
    super(javaPlugin.getLogger());
    this.plugin = javaPlugin;
  }

  @Override
  protected Connection createConnection() throws SQLException {
    try {
      ensureDataFolderExists();
      Class.forName("org.sqlite.JDBC");
      return DriverManager.getConnection(
          "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/accounts.db");
    } catch (ClassNotFoundException e) {
      throw new SQLException(e);
    }
  }

  private void ensureDataFolderExists() {
    if (!plugin.getDataFolder().exists()) {
      plugin.getDataFolder().mkdirs();
    }
  }
}