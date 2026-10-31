package cc.baka9.catseedlogin.bukkit.database;

import cc.baka9.catseedlogin.bukkit.object.LoginPlayer;
import cc.baka9.catseedlogin.bukkit.object.TotpRecoveryCode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/** 抽象数据库操作类，提供通用的 CRUD 方法。 子类需实现 getConnection() 和 closeConnection() 以提供具体连接。 */
public abstract class SQL {
  protected Logger logger;

  public SQL(Logger logger) {
    this.logger = logger;
  }

  /** 初始化数据库表结构。 */
  public synchronized void init() throws SQLException {
    try {
      flush(
          new BufferStatement(
              "CREATE TABLE IF NOT EXISTS accounts (name CHAR(255), password CHAR(255), email"
                  + " CHAR(255), ips CHAR(255), lastAction TIMESTAMP, location CHAR(255) DEFAULT"
                  + " NULL)"));
    } catch (SQLException e) {
      logger.severe("Failed to create accounts table: " + e.getMessage());
      throw e;
    }

    addColumnIfMissing("email");
    addColumnIfMissing("ips");
    addColumnIfMissing("location");
    addColumnIfMissing("totp_secret");
    addColumnIfMissing("totp_enabled");

    createRecoveryCodeTable();
  }

  /** 恢复码单独建表：一个玩家多条码，塞进 accounts 单列会让「标记单条已用」变成读-改-写整串。 */
  private void createRecoveryCodeTable() {
    try {
      flush(
          new BufferStatement(
              "CREATE TABLE IF NOT EXISTS totp_recovery_codes (name CHAR(255), code_hash"
                  + " CHAR(255), salt CHAR(255), used_at TIMESTAMP NULL)"));
    } catch (SQLException e) {
      // 两步验证是可选功能，建表失败不应该拖垮整个插件
      logger.severe("Failed to create totp_recovery_codes table: " + e.getMessage());
    }
  }

  private void addColumnIfMissing(String columnName) {
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
    try (ResultSet rs =
        getConnection().getMetaData().getColumns(null, null, tableName, columnName)) {
      return rs.next();
    } catch (SQLException e) {
      return false;
    }
  }

  /**
   * 添加登录玩家。
   *
   * @param lp 登录玩家对象
   */
  public synchronized void add(LoginPlayer lp) {
    try {
      flush(
          new BufferStatement(
              "INSERT INTO accounts (name, password, lastAction, email, ips, location) VALUES (?,"
                  + " ?, ?, ?, ?, ?)",
              lp.getName(),
              lp.getPassword(),
              new Date(),
              lp.getEmail(),
              lp.getIps(),
              lp.getLocation()));
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
  public synchronized void del(String name) {
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
  public synchronized void edit(LoginPlayer lp) {
    try {
      flush(
          new BufferStatement(
              "UPDATE accounts SET password = ?, lastAction = ?, email = ?, ips = ?, location = ?"
                  + " WHERE name = ?",
              lp.getPassword(),
              new Date(),
              lp.getEmail(),
              lp.getIps(),
              lp.getLocation(),
              lp.getName()));
    } catch (SQLException e) {
      logger.severe("Failed to edit player: " + lp.getName() + " - " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  /**
   * 更新玩家位置信息。
   *
   * @param name 玩家名称
   * @param location 位置字符串
   */
  public synchronized void updateLocation(String name, String location) {
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
  public synchronized String getLocation(String name) {
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
  public synchronized LoginPlayer get(String name) {
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
   * @param sql 查询 SQL
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
  public synchronized List<LoginPlayer> getAll() {
    try (PreparedStatement ps =
            new BufferStatement("SELECT * FROM accounts").prepareStatement(getConnection());
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
  public synchronized List<LoginPlayer> getLikeByIp(String ip) {
    String likePattern = "%" + ip + "%";
    try (PreparedStatement ps =
            new BufferStatement("SELECT * FROM accounts WHERE ips LIKE ?", likePattern)
                .prepareStatement(getConnection());
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
    lp.setTotpSecret(resultSet.getString("totp_secret"));
    lp.setTotpEnabled("1".equals(resultSet.getString("totp_enabled")));
    return lp;
  }

  /**
   * 更新玩家的 TOTP 密钥。
   *
   * @param secret 经 {@link cc.baka9.catseedlogin.common.util.SecretCodec} 加密后的密钥，传 null 表示解绑
   */
  public synchronized void updateTotpSecret(String name, String secret) {
    try {
      flush(
          new BufferStatement(
              "UPDATE accounts SET totp_secret = ? WHERE name = ?", secret, name));
    } catch (SQLException e) {
      logger.severe("Failed to update totp secret for player: " + name + " - " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  /** 更新两步验证启用状态。 */
  public synchronized void updateTotpEnabled(String name, boolean enabled) {
    try {
      flush(
          new BufferStatement(
              "UPDATE accounts SET totp_enabled = ? WHERE name = ?", enabled ? "1" : "0", name));
    } catch (SQLException e) {
      logger.severe("Failed to update totp state for player: " + name + " - " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  /** 整批替换玩家的恢复码，旧码一并作废。 */
  public synchronized void saveRecoveryCodes(String name, List<TotpRecoveryCode> codes) {
    try {
      flush(new BufferStatement("DELETE FROM totp_recovery_codes WHERE name = ?", name));
      if (codes == null) {
        return;
      }
      for (TotpRecoveryCode code : codes) {
        if (code == null) {
          continue;
        }
        flush(
            new BufferStatement(
                "INSERT INTO totp_recovery_codes (name, code_hash, salt) VALUES (?, ?, ?)",
                name,
                code.getCodeHash(),
                code.getSalt()));
      }
    } catch (SQLException e) {
      logger.severe("Failed to save recovery codes for player: " + name + " - " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  /** 查询玩家的全部恢复码（含已使用的）。 */
  public synchronized List<TotpRecoveryCode> getRecoveryCodes(String name) {
    List<TotpRecoveryCode> codes = new ArrayList<>();
    String sql = "SELECT name, code_hash, salt, used_at FROM totp_recovery_codes WHERE name = ?";
    try (PreparedStatement ps = new BufferStatement(sql, name).prepareStatement(getConnection());
        ResultSet resultSet = ps.executeQuery()) {
      while (resultSet.next()) {
        java.sql.Timestamp usedAt = resultSet.getTimestamp("used_at");
        codes.add(
            new TotpRecoveryCode(
                resultSet.getString("name"),
                resultSet.getString("code_hash"),
                resultSet.getString("salt"),
                usedAt == null ? null : usedAt.getTime()));
      }
    } catch (SQLException e) {
      logger.severe("Failed to get recovery codes for player: " + name + " - " + e.getMessage());
    }
    return codes;
  }

  /**
   * 在玩家未使用的恢复码里查找与输入匹配的那一。
   *
   * @param rawCode 玩家输入的原始恢复码（可带连字符、大小写不限）
   * @return 匹配到的恢复码，没有匹配返回 null
   */
  public synchronized TotpRecoveryCode findUnusedRecoveryCode(String name, String rawCode) {
    for (TotpRecoveryCode code : getRecoveryCodes(name)) {
      if (code.isUsed()) {
        continue;
      }
      String candidate = TotpRecoveryCode.hash(code.getSalt(), rawCode);
      if (candidate != null && candidate.equals(code.getCodeHash())) {
        return code;
      }
    }
    return null;
  }

  /** 把恢复码标记为已使用。 */
  public synchronized void markRecoveryCodeUsed(String name, String codeHash) {
    try {
      flush(
          new BufferStatement(
              "UPDATE totp_recovery_codes SET used_at = ? WHERE name = ? AND code_hash = ? AND"
                  + " used_at IS NULL",
              new Date(),
              name,
              codeHash));
    } catch (SQLException e) {
      logger.severe(
          "Failed to mark recovery code used for player: " + name + " - " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  /** 作废玩家的全部恢复码（关闭两步验证或重新绑定时使用）。 */
  public synchronized void deleteRecoveryCodes(String name) {
    try {
      flush(new BufferStatement("DELETE FROM totp_recovery_codes WHERE name = ?", name));
    } catch (SQLException e) {
      logger.severe("Failed to delete recovery codes for player: " + name + " - " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  /**
   * 获取数据库连接。
   *
   * @return Connection 对象
   */
  public abstract Connection getConnection() throws SQLException;

  /** 关闭数据库连接。 */
  public abstract void closeConnection();

  /**
   * 执行缓冲语句。
   *
   * @param bufferStatement 缓冲 SQL 语句
   */
  public synchronized void flush(BufferStatement bufferStatement) throws SQLException {
    try (PreparedStatement ps = bufferStatement.prepareStatement(getConnection())) {
      ps.executeUpdate();
    } catch (SQLException e) {
      logger.severe("Failed to execute flush: " + e.getMessage());
      throw e;
    }
  }
}
