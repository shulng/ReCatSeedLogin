package cc.baka9.catseedlogin.bukkit.database

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException
import org.bukkit.plugin.java.JavaPlugin

class SQLite(private val plugin: JavaPlugin) : SQL(plugin.logger) {
    private var connection: Connection? = null
    @Synchronized @Throws(SQLException::class)
    override fun getConnection(): Connection {
        if (isConnectionValid()) return connection!!
        closeConnection(); connection = createConnection(); return connection!!
    }
    private fun isConnectionValid(): Boolean {
        val current = connection ?: return false
        if (current.isClosed) return false
        return try { current.prepareStatement("SELECT 1").use { it.executeQuery().use { _ -> } }; true } catch (_: SQLException) { false }
    }
    @Throws(SQLException::class)
    private fun createConnection(): Connection {
        ensureDataFolderExists()
        return try { Class.forName("org.sqlite.JDBC"); DriverManager.getConnection("jdbc:sqlite:${plugin.dataFolder.absolutePath}/accounts.db") }
        catch (e: ClassNotFoundException) { throw SQLException(e) }
    }
    private fun ensureDataFolderExists() { if (!plugin.dataFolder.exists()) plugin.dataFolder.mkdirs() }
    @Synchronized override fun closeConnection() {
        try { if (connection != null && !connection!!.isClosed) connection!!.close() } catch (e: SQLException) { logger.warning("关闭SQLite连接时出错: ${e.message}") }
        connection = null
    }
}
