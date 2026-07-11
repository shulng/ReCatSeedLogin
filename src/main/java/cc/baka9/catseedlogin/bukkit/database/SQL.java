package cc.baka9.catseedlogin.bukkit.database;

import cc.baka9.catseedlogin.common.model.LoginPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * 抽象数据库操作类，提供通用的 CRUD 方法。
 * 子类需实现 getConnection() 和 closeConnection() 以提供具体连接。
 */
public abstract class SQL {
    protected Logger logger;

    public SQL(Logger logger) {
        this.logger = logger;
    }

    /**
     * 初始化数据库表结构。
     */
    public void init() throws SQLException {
        try {
            flush(new BufferStatement("CREATE TABLE IF NOT EXISTS accounts (name CHAR(255), password CHAR(255), email CHAR(255), ips CHAR(255), lastAction TIMESTAMP, location CHAR(255) DEFAULT NULL)"));
        } catch (SQLException e) {
            logger.severe("Failed to create accounts table: " + e.getMessage());
            throw e;
        }

        addColumnIfMissing("email");
        addColumnIfMissing("ips");
        addColumnIfMissing("location");
    }

    private static final java.util.Set<String> ALLOWED_COLUMNS = java.util.Collections.unmodifiableSet(
            new java.util.HashSet<>(java.util.Arrays.asList("email", "ips", "location")));

    private void addColumnIfMissing(String columnName) {
        if (!ALLOWED_COLUMNS.contains(columnName)) {
            logger.warning("Refused to add disallowed column: " + columnName);
            return;
        }
        if (columnExists("accounts", columnName)) {
            return;
        }
        try {
            flush(new BufferStatement("ALTER TABLE accounts ADD " + columnName + " CHAR(255)"));
        } catch (SQLException e) {
            logger.warning("Failed to add column " + columnName + ": " + e.getMessage());
        }
    }

    private boolean columnExists(String tableName, String columnName) {
        try {
            Connection conn = getConnection();
            try (ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, columnName)) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * 添加登录玩家。
     *
     * @param lp 登录玩家对象
     */
    public void add(LoginPlayer lp) {
        try {
            flush(new BufferStatement("INSERT INTO accounts (name, password, lastAction, email, ips, location) VALUES (?, ?, ?, ?, ?, ?)",
                lp.getName(), lp.getPassword(), new Date(), lp.getEmail(), lp.getIps(), lp.getLocation()));
        } catch (SQLException e) {
            logger.severe("Failed to add player: " + lp.getName() + " - " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除登录玩家。
     *
     * @param name 玩家名称
     */
    public void del(String name) {
        try {
            flush(new BufferStatement("DELETE FROM accounts WHERE name = ?", name));
        } catch (SQLException e) {
            logger.severe("Failed to delete player: " + name + " - " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 更新登录玩家信息。
     *
     * @param lp 登录玩家对象
     */
    public void edit(LoginPlayer lp) {
        try {
            flush(new BufferStatement("UPDATE accounts SET password = ?, lastAction = ?, email = ?, ips = ?, location = ? WHERE name = ?",
                lp.getPassword(), new Date(), lp.getEmail(), lp.getIps(), lp.getLocation(), lp.getName()));
        } catch (SQLException e) {
            logger.severe("Failed to edit player: " + lp.getName() + " - " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 更新玩家位置信息。
     *
     * @param name     玩家名称
     * @param location 位置字符串
     */
    public void updateLocation(String name, String location) {
        try {
            flush(new BufferStatement("UPDATE accounts SET location = ? WHERE name = ?", location, name));
        } catch (SQLException e) {
            logger.severe("Failed to update location for player: " + name + " - " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取玩家位置信息。
     *
     * @param name 玩家名称
     * @return 位置字符串，未找到时返回 null
     */
    public String getLocation(String name) {
        try {
            return queryForString("SELECT location FROM accounts WHERE name = ?", name);
        } catch (Exception e) {
            logger.severe("Failed to get location for player: " + name + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * 根据名称获取登录玩家信息。
     *
     * @param name 玩家名称
     * @return LoginPlayer 对象，未找到时返回 null
     */
    public LoginPlayer get(String name) {
        String sql = "SELECT * FROM accounts WHERE name = ?";
        try (PreparedStatement ps = new BufferStatement(sql, name).prepareStatement(getConnection());
             ResultSet resultSet = ps.executeQuery()) {
            return mapLoginPlayerOrNull(resultSet);
        } catch (SQLException e) {
            logger.severe("Failed to get player: " + name + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * 执行查询并返回单行单列字符串结果。
     *
     * @param sql    查询 SQL
     * @param params 参数
     * @return 查询结果字符串，未找到时返回 null
     */
    private String queryForString(String sql, Object... params) {
        try (PreparedStatement ps = new BufferStatement(sql, params).prepareStatement(getConnection());
             ResultSet resultSet = ps.executeQuery()) {
            return resultSet.next() ? resultSet.getString(1) : null;
        } catch (SQLException e) {
            logger.severe("Failed to query: " + sql + " - " + e.getMessage());
            return null;
        }
    }

    private LoginPlayer mapLoginPlayerOrNull(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            return mapLoginPlayer(resultSet);
        }
        return null;
    }

    /**
     * 获取所有登录玩家。
     *
     * @return 登录玩家列表
     */
    public List<LoginPlayer> getAll() {
        try (PreparedStatement ps = new BufferStatement("SELECT * FROM accounts").prepareStatement(getConnection());
             ResultSet resultSet = ps.executeQuery()) {
            List<LoginPlayer> lps = new ArrayList<>();
            while (resultSet.next()) {
                lps.add(mapLoginPlayer(resultSet));
            }
            return lps;
        } catch (SQLException e) {
            logger.severe("Failed to get all players: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 根据 IP 模糊匹配查询登录玩家。
     *
     * @param ip IP 地址
     * @return 匹配的登录玩家列表
     */
    public List<LoginPlayer> getLikeByIp(String ip) {
        String likePattern = "%" + ip + "%";
        try (PreparedStatement ps = new BufferStatement("SELECT * FROM accounts WHERE ips LIKE ?", likePattern).prepareStatement(getConnection());
             ResultSet resultSet = ps.executeQuery()) {
            List<LoginPlayer> lps = new ArrayList<>();
            while (resultSet.next()) {
                lps.add(mapLoginPlayer(resultSet));
            }
            return lps;
        } catch (SQLException e) {
            logger.severe("Failed to query players by IP: " + ip + " - " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private LoginPlayer mapLoginPlayer(ResultSet resultSet) throws SQLException {
        LoginPlayer lp = new LoginPlayer(resultSet.getString("name"), resultSet.getString("password"));
        java.sql.Timestamp ts = resultSet.getTimestamp("lastAction");
        lp.setLastAction(ts != null ? ts.getTime() : 0L);
        lp.setEmail(resultSet.getString("email"));
        lp.setIps(resultSet.getString("ips"));
        lp.setLocation(resultSet.getString("location"));
        return lp;
    }

    /**
     * 获取数据库连接。
     *
     * @return Connection 对象
     */
    public abstract Connection getConnection() throws SQLException;

    /**
     * 关闭数据库连接。
     */
    public abstract void closeConnection();

    /**
     * 执行缓冲语句。
     *
     * @param bufferStatement 缓冲 SQL 语句
     */
    public void flush(BufferStatement bufferStatement) throws SQLException {
        try (PreparedStatement ps = bufferStatement.prepareStatement(getConnection())) {
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Failed to execute flush: " + e.getMessage());
            throw e;
        }
    }
}
