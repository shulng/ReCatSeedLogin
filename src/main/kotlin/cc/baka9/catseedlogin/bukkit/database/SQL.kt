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
        addColumnIfMissing("name_key", "CHAR(64)")
        ensureNameKeyConstraint()
        createRecoveryCodeTable()
        validateAccountNames()
    }

    private fun ensureNameKeyConstraint() {
        try {
            flush(BufferStatement("UPDATE accounts SET name_key = LOWER(name) WHERE name_key IS NULL OR name_key = ''"))
            if (!indexExists("accounts", "accounts_name_key_uq")) {
                flush(BufferStatement("CREATE UNIQUE INDEX accounts_name_key_uq ON accounts (name_key)"))
            }
        } catch (exception: SQLException) {
            logger.severe("Failed to enforce unique account names: ${exception.message}")
            throw exception
        }
    }

    private fun indexExists(tableName: String, indexName: String): Boolean = try {
        getConnection().metaData.getIndexInfo(null, null, tableName, true, false).use { resultSet ->
            generateSequence { if (resultSet.next()) resultSet.getString("INDEX_NAME") else null }
                .any { it.equals(indexName, ignoreCase = true) }
        }
    } catch (_: SQLException) {
        false
    }

    /** Cache lookup is case-insensitive, so duplicate names would make authentication ambiguous. */
    private fun validateAccountNames() {
        try {
            BufferStatement("SELECT LOWER(name) FROM accounts GROUP BY LOWER(name) HAVING COUNT(*) > 1")
                .prepareStatement(getConnection()).use { statement ->
                    statement.executeQuery().use { resultSet ->
                        if (resultSet.next()) {
                            throw SQLException("accounts contains duplicate names differing only by case")
                        }
                    }
                }
        } catch (exception: SQLException) {
            logger.severe("Account name validation failed: ${exception.message}")
            throw exception
        }
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
            logger.severe("Failed to create recovery-code table: ${exception.message}")
            throw exception
        }
    }

    private fun addColumnIfMissing(columnName: String, columnType: String = "CHAR(255)") {
        if (columnExists("accounts", columnName)) return
        try {
            flush(BufferStatement("ALTER TABLE accounts ADD $columnName $columnType"))
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
            BufferStatement("SELECT 1 FROM accounts WHERE LOWER(name) = LOWER(?) LIMIT 1", player.name)
                .prepareStatement(getConnection()).use { statement ->
                    statement.executeQuery().use { resultSet ->
                        if (resultSet.next()) throw SQLException("account name already exists: ${player.name}")
                    }
                }
            flush(
                BufferStatement(
                    "INSERT INTO accounts (name, name_key, password, lastAction, email, ips, location) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    player.name,
                    player.name.lowercase(),
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

    /** Atomically checks the per-IP limit and inserts an account. Returns matching accounts when limited. */
    @Synchronized
    fun addWithIpLimit(player: LoginPlayer, ip: String, limit: Int, enforceLimit: Boolean): List<LoginPlayer>? {
        val connection = getConnection()
        val previousAutoCommit = connection.autoCommit
        val lockName = "catseedlogin:ip:$ip"
        var mysqlLock = false
        try {
            if (connection.metaData.databaseProductName.contains("mysql", ignoreCase = true)) {
                connection.prepareStatement("SELECT GET_LOCK(?, 10)").use {
                    it.setString(1, lockName)
                    it.executeQuery().use { result ->
                        if (!result.next() || result.getInt(1) != 1) throw SQLException("unable to acquire registration lock")
                    }
                }
                mysqlLock = true
            }
            connection.autoCommit = false
            val matches = if (enforceLimit) getLikeByIp(ip) else emptyList()
            if (enforceLimit && matches.size >= limit) {
                connection.rollback()
                return matches
            }
            add(player)
            connection.commit()
            return null
        } catch (exception: Exception) {
            try { connection.rollback() } catch (_: SQLException) { }
            throw if (exception is RuntimeException) exception else RuntimeException(exception)
        } finally {
            try { connection.autoCommit = previousAutoCommit } catch (_: SQLException) { }
            if (mysqlLock) {
                try { connection.prepareStatement("SELECT RELEASE_LOCK(?)").use { it.setString(1, lockName); it.executeQuery().use { } } } catch (_: SQLException) { }
            }
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

    /** Upgrade a legacy hash only if no concurrent password change replaced it. */
    @Synchronized
    fun updatePasswordIfCurrent(name: String, oldPassword: String, newPassword: String): Boolean {
        return try {
            BufferStatement(
                "UPDATE accounts SET password = ?, lastAction = ? WHERE name = ? AND password = ?",
                newPassword, Date(), name, oldPassword
            ).prepareStatement(getConnection()).use { it.executeUpdate() == 1 }
        } catch (exception: SQLException) {
            logger.warning("Failed to upgrade password hash for player: $name - ${exception.message}")
            false
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
            throw IllegalStateException("无法读取账号数据", exception)
        }
        return players
    }

    @Synchronized
    fun getLikeByIp(ip: String): List<LoginPlayer> {
        val players = mutableListOf<LoginPlayer>()
        try {
            BufferStatement(
                "SELECT * FROM accounts WHERE ips = ? OR ips LIKE ? OR ips LIKE ? OR ips LIKE ?",
                ip, "$ip;%", "%;$ip", "%;$ip;%"
            )
                .prepareStatement(getConnection()).use { statement ->
                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next()) players += mapLoginPlayer(resultSet)
                    }
                }
        } catch (exception: SQLException) {
            logger.severe("Failed to query players by IP: $ip - ${exception.message}")
            throw IllegalStateException("无法查询IP注册限制", exception)
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
    fun saveTwoFactorState(name: String, encryptedSecret: String, codes: List<TotpRecoveryCode>): Boolean =
        withTransaction { connection ->
            connection.prepareStatement("UPDATE accounts SET totp_secret = ?, totp_enabled = ? WHERE name = ?").use {
                it.setString(1, encryptedSecret); it.setString(2, "1"); it.setString(3, name)
                if (it.executeUpdate() != 1) return@withTransaction false
            }
            connection.prepareStatement("DELETE FROM totp_recovery_codes WHERE name = ?").use {
                it.setString(1, name); it.executeUpdate()
            }
            connection.prepareStatement("INSERT INTO totp_recovery_codes (name, code_hash) VALUES (?, ?)").use { statement ->
                codes.forEach { code -> statement.setString(1, name); statement.setString(2, code.codeHash); statement.addBatch() }
                statement.executeBatch()
            }
            true
        }

    @Synchronized
    fun disableTwoFactorState(name: String): Boolean =
        withTransaction { connection ->
            connection.prepareStatement("UPDATE accounts SET totp_secret = NULL, totp_enabled = ? WHERE name = ?").use {
                it.setString(1, "0"); it.setString(2, name)
                if (it.executeUpdate() != 1) return@withTransaction false
            }
            connection.prepareStatement("UPDATE totp_recovery_codes SET used_at = ? WHERE name = ? AND used_at IS NULL").use {
                it.setTimestamp(1, java.sql.Timestamp(System.currentTimeMillis())); it.setString(2, name); it.executeUpdate()
            }
            true
        }

    @Synchronized
    fun recoverTwoFactorState(name: String): Boolean = disableTwoFactorState(name)

    private fun <T> withTransaction(block: (Connection) -> T): T {
        val connection = getConnection()
        val previousAutoCommit = connection.autoCommit
        try {
            connection.autoCommit = false
            val result = block(connection)
            connection.commit()
            return result
        } catch (exception: SQLException) {
            try { connection.rollback() } catch (_: SQLException) { }
            logger.severe("Failed to update two-factor state: ${exception.message}")
            @Suppress("UNCHECKED_CAST")
            return false as T
        } finally {
            try { connection.autoCommit = previousAutoCommit } catch (_: SQLException) { }
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
    fun consumeRecoveryCode(name: String, rawCode: String): TotpRecoveryCode? {
        return try {
            val matched = getRecoveryCodes(name).firstOrNull {
                !it.isUsed() && TotpRecoveryCode.matches(name, rawCode, it.codeHash)
            } ?: return null
            val updated = BufferStatement(
                "UPDATE totp_recovery_codes SET used_at = ? WHERE name = ? AND code_hash = ? AND used_at IS NULL",
                Date(), name, matched.codeHash
            ).prepareStatement(getConnection()).use { it.executeUpdate() }
            if (updated == 1) matched else null
        } catch (exception: SQLException) {
            logger.severe("Failed to consume recovery code for player: $name - ${exception.message}")
            throw RuntimeException(exception)
        }
    }

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
