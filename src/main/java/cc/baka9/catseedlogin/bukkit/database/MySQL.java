package cc.baka9.catseedlogin.bukkit.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.bukkit.plugin.java.JavaPlugin;
import cc.baka9.catseedlogin.bukkit.Config;

public class MySQL extends SQL {
    private Connection connection;

    public MySQL(JavaPlugin javaPlugin) {
        super(javaPlugin.getLogger());
    }

    @Override
    public synchronized Connection getConnection() throws SQLException {
        if (isConnectionValid()) {
            return this.connection;
        }
        closeConnection();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(
                "jdbc:mysql://" + Config.MySQL.Host + ":" + Config.MySQL.Port + "/" + Config.MySQL.Database + "?characterEncoding=UTF-8",
                Config.MySQL.User, Config.MySQL.Password
            );
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
            e.printStackTrace();
        }
        this.connection = null;
    }
}
