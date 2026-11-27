package cc.baka9.catseedlogin.bukkit.database

import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayer
import cc.baka9.catseedlogin.bukkit.`object`.TotpRecoveryCode
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.Date
import java.util.logging.Logger

/** Shared CRUD operations used by the concrete database implementations. */
abstract class SQL(protected val logger: Logger) {
    @Synchronized
    @Throws(SQLException::class)
    fun init() {
        try {
            flush(
                BufferStatement(
                    "CREATE TABLE IF NOT EXISTS accounts (name CHAR(255), password CHAR(255), email " +
                        "CHAR(255), ips CHAR(255), lastAction TIMESTAMP, location CHAR(255) DEFAULT NULL)"
                )
            )
        } catch (exception: SQLException) {
            logger.severe("Failed to create accounts table: ${exception.message}")
            throw exception
        }

        addColumnIfMissing("email")
        addColumnIfMissing("ips")
        addColumnIfMissing("location")
        addColumnIfMissing("totp_secret")
        addColumnIfMissing("totp_enabled")
        createRecoveryCodeTable()
    }

    private fun createRecoveryCodeTable() {
        try {
            flush(
                BufferStatement(
                    "CREATE TABLE IF NOT EXISTS totp_recovery_codes (name CHAR(255), code_hash " +
                        "CHAR(255), used_at TIMESTAMP NULL)"
                )
            )
        } catch (exception: SQLException) {
            // Two-factor authentication is optional, so failure to create this table is non-fatal.
            logger.severe("Failed to create recovery-code table: ${exception.message}")
        }
    }

    private fun addColumnIfMissing(columnName: String) {
        if (columnExists("accounts", columnName)) return
        try {
            flush(BufferStatement("ALTER TABLE accounts ADD $columnName CHAR(255)"))
        } catch (exception: SQLException) {
            logger.warning("Failed to add column $columnName: ${exception.message}")
        }
    }

    private fun columnExists(tableName: String, columnName: String): Boolean = try {
        getConnection().metaData.getColumns(null, null, tableName, columnName).use { it.next() }
    } catch (_: SQLException) {
        false
    }

    @Synchronized
    fun add(player: LoginPlayer) {
        try {
            flush(
                BufferStatement(
                    "INSERT INTO accounts (name, password, lastAction, email, ips, location) VALUES (?, ?, ?, ?, ?, ?)",
                    player.name,
                    player.password,
                    Date(),
                    player.email,
                    player.ips,
                    player.location
                )
            )
        } catch (exception: SQLException) {
            logger.severe("Failed to add player: ${player.name} - ${exception.message}")
            throw RuntimeException(exception)
        }
    }

    @Synchronized
    fun del(name: String) {
        try {
            flush(BufferStatement("DELETE FROM accounts WHERE name = ?", name))
        } catch (exception: SQLException) {
            logger.severe("Failed to delete player: $name - ${exception.message}")
            throw RuntimeException(exception)
        }
    }

    @Synchronized
    fun edit(player: LoginPlayer) {
        try {
            flush(
                BufferStatement(
                    "UPDATE accounts SET password = ?, lastAction = ?, email = ?, ips = ?, location = ? WHERE name = ?",
                    player.password,
                    Date(),
                    player.email,
                    player.ips,
                    player.location,
                    player.name
                )
            )
        } catch (exception: SQLException) {
            logger.severe("Failed to edit player: ${player.name} - ${exception.message}")
            throw RuntimeException(exception)
        }
    }

    @Synchronized
    fun updateLocation(name: String, location: String) {
        try {
            flush(BufferStatement("UPDATE accounts SET location = ? WHERE name = ?", location, name))
        } catch (exception: SQLException) {
            logger.severe("Failed to update location for player: $name - ${exception.message}")
            throw RuntimeException(exception)
        }
    }

    @Synchronized
    fun getLocation(name: String): String? = try {
        queryForString("SELECT location FROM accounts WHERE name = ?", name)
    } catch (exception: Exception) {
        logger.severe("Failed to get location for player: $name - ${exception.message}")
        null
    }

    @Synchronized
    fun get(name: String): LoginPlayer? {
        try {
            BufferStatement("SELECT * FROM accounts WHERE name = ?", name)
                .prepareStatement(getConnection()).use { statement ->
                    statement.executeQuery().use { resultSet -> return mapLoginPlayerOrNull(resultSet) }
                }
        } catch (exception: SQLException) {
            logger.severe("Failed to get player: $name - ${exception.message}")
            return null
        }
    }

    private fun queryForString(sql: String, vararg params: Any?): String? {
        try {
            BufferStatement(sql, *params).prepareStatement(getConnection()).use { statement ->
                statement.executeQuery().use { resultSet ->
                    return if (resultSet.next()) resultSet.getString(1) else null
                }
            }
        } catch (exception: SQLException) {
            logger.severe("Failed to query: $sql - ${exception.message}")
            return null
        }
    }

    private fun mapLoginPlayerOrNull(resultSet: ResultSet): LoginPlayer? =
        if (resultSet.next()) mapLoginPlayer(resultSet) else null

    @Synchronized
    fun getAll(): List<LoginPlayer> {
        val players = mutableListOf<LoginPlayer>()
        try {
            BufferStatement("SELECT * FROM accounts").prepareStatement(getConnection()).use { statement ->
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) players += mapLoginPlayer(resultSet)
                }
            }
        } catch (exception: SQLException) {
            logger.severe("Failed to get all players: ${exception.message}")
        }
        return players
    }

    @Synchronized
    fun getLikeByIp(ip: String): List<LoginPlayer> {
        val players = mutableListOf<LoginPlayer>()
        try {
            BufferStatement("SELECT * FROM accounts WHERE ips LIKE ?", "%$ip%")
                .prepareStatement(getConnection()).use { statement ->
                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next()) players += mapLoginPlayer(resultSet)
                    }
                }
        } catch (exception: SQLException) {
            logger.severe("Failed to query players by IP: $ip - ${exception.message}")
        }
        return players
    }

    private fun mapLoginPlayer(resultSet: ResultSet): LoginPlayer {
        val player = LoginPlayer(resultSet.getString("name"), resultSet.getString("password"))
        player.lastAction = resultSet.getTimestamp("lastAction")?.time ?: 0L
        player.email = resultSet.getString("email")
        player.ips = resultSet.getString("ips")
        player.location = resultSet.getString("location")
        player.totpSecret = resultSet.getString("totp_secret")
        player.totpEnabled = resultSet.getString("totp_enabled") == "1"
        return player
    }

    @Synchronized
    fun updateTotpSecret(name: String, secret: String?) {
        try {
            flush(BufferStatement("UPDATE accounts SET totp_secret = ? WHERE name = ?", secret, name))
        } catch (exception: SQLException) {
            logger.severe("Failed to update totp secret for player: $name - ${exception.message}")
            throw RuntimeException(exception)
        }
    }

    @Synchronized
    fun updateTotpEnabled(name: String, enabled: Boolean) {
        try {
            flush(BufferStatement("UPDATE accounts SET totp_enabled = ? WHERE name = ?", if (enabled) "1" else "0", name))
        } catch (exception: SQLException) {
            logger.severe("Failed to update totp state for player: $name - ${exception.message}")
            throw RuntimeException(exception)
        }
    }

    @Synchronized
    fun saveRecoveryCodes(name: String, codes: List<TotpRecoveryCode?>?) {
        try {
            flush(BufferStatement("DELETE FROM totp_recovery_codes WHERE name = ?", name))
            if (codes == null) return
            for (code in codes) {
                if (code == null) continue
                flush(
                    BufferStatement(
                        "INSERT INTO totp_recovery_codes (name, code_hash) VALUES (?, ?)",
                        name,
                        code.codeHash
                    )
                )
            }
        } catch (exception: SQLException) {
            logger.severe("Failed to save recovery codes for player: $name - ${exception.message}")
            throw RuntimeException(exception)
        }
    }

    @Synchronized
    fun getRecoveryCodes(name: String): List<TotpRecoveryCode> {
        val codes = mutableListOf<TotpRecoveryCode>()
        val sql = "SELECT name, code_hash, used_at FROM totp_recovery_codes WHERE name = ?"
        try {
            BufferStatement(sql, name).prepareStatement(getConnection()).use { statement ->
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        codes += TotpRecoveryCode(
                            resultSet.getString("name"),
                            resultSet.getString("code_hash"),
                            resultSet.getTimestamp("used_at")?.time
                        )
                    }
                }
            }
        } catch (exception: SQLException) {
            logger.severe("Failed to get recovery codes for player: $name - ${exception.message}")
        }
        return codes
    }

    @Synchronized
    fun findUnusedRecoveryCode(name: String, rawCode: String): TotpRecoveryCode? =
        getRecoveryCodes(name).firstOrNull { !it.isUsed() && TotpRecoveryCode.matches(name, rawCode, it.codeHash) }

    @Synchronized
    fun markRecoveryCodeUsed(name: String, codeHash: String) {
        try {
            flush(
                BufferStatement(
                    "UPDATE totp_recovery_codes SET used_at = ? WHERE name = ? AND code_hash = ? AND used_at IS NULL",
                    Date(), name, codeHash
                )
            )
        } catch (exception: SQLException) {
            logger.severe("Failed to mark recovery code used for player: $name - ${exception.message}")
            throw RuntimeException(exception)
        }
    }

    @Synchronized
    fun invalidateRecoveryCodes(name: String) {
        try {
            flush(
                BufferStatement(
                    "UPDATE totp_recovery_codes SET used_at = ? WHERE name = ? AND used_at IS NULL",
                    Date(), name
                )
            )
        } catch (exception: SQLException) {
            logger.severe("Failed to invalidate recovery codes for player: $name - ${exception.message}")
            throw RuntimeException(exception)
        }
    }

    @Synchronized
    fun deleteRecoveryCodes(name: String) {
        try {
            flush(BufferStatement("DELETE FROM totp_recovery_codes WHERE name = ?", name))
        } catch (exception: SQLException) {
            logger.severe("Failed to delete recovery codes for player: $name - ${exception.message}")
            throw RuntimeException(exception)
        }
    }

    @Throws(SQLException::class)
    abstract fun getConnection(): Connection

    abstract fun closeConnection()

    @Synchronized
    @Throws(SQLException::class)
    fun flush(bufferStatement: BufferStatement) {
        try {
            bufferStatement.prepareStatement(getConnection()).use { it.executeUpdate() }
        } catch (exception: SQLException) {
            logger.severe("Failed to execute flush: ${exception.message}")
            throw exception
        }
    }
}
