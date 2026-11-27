package cc.baka9.catseedlogin.bukkit.database

import cc.baka9.catseedlogin.bukkit.Config
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import org.bukkit.plugin.java.JavaPlugin

class MySQL(javaPlugin: JavaPlugin) : SQL(javaPlugin.logger) {
    private var connection: Connection? = null
    @Synchronized @Throws(SQLException::class)
    override fun getConnection(): Connection {
        if (isConnectionValid()) return connection!!
        closeConnection()
        return try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            DriverManager.getConnection("jdbc:mysql://${Config.MySQL.Host}:${Config.MySQL.Port}/${Config.MySQL.Database}?characterEncoding=UTF-8", Config.MySQL.User, Config.MySQL.Password).also { connection = it }
        } catch (e: ClassNotFoundException) { throw SQLException(e) }
    }
    private fun isConnectionValid(): Boolean {
        val current = connection ?: return false
        if (current.isClosed) return false
        return try { current.prepareStatement("SELECT 1").use { it.executeQuery().use { _ -> } }; true } catch (_: SQLException) { false }
    }
    @Synchronized override fun closeConnection() {
        try { if (connection != null && !connection!!.isClosed) connection!!.close() } catch (e: SQLException) { e.printStackTrace() }
        connection = null
    }
}
