package cc.baka9.catseedlogin.bukkit.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import org.bukkit.plugin.java.JavaPlugin;

public class SQLite extends SQL {
    private Connection connection;
    private final JavaPlugin plugin;

    public SQLite(JavaPlugin javaPlugin) {
        super(javaPlugin.getLogger());
        this.plugin = javaPlugin;
    }

    @Override
    public synchronized Connection getConnection() throws SQLException {
        if (isConnectionValid()) {
            return connection;
        }
        closeConnection();
        connection = createConnection();
        return connection;
    }

    private boolean isConnectionValid() throws SQLException {
        if (connection == null || connection.isClosed()) {
            return false;
        }
        try (PreparedStatement ps = connection.prepareStatement("SELECT 1")) {
            ps.executeQuery();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private Connection createConnection() throws SQLException {
        try {
            ensureDataFolderExists();
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/accounts.db");
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }
    }

    private void ensureDataFolderExists() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
    }

    @Override
    public synchronized void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.warning("关闭SQLite连接时出错: " + e.getMessage());
        }
        connection = null;
    }
}
