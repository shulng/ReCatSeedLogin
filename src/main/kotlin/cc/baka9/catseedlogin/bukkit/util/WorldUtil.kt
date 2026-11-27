package cc.baka9.catseedlogin.bukkit.util

import java.io.BufferedInputStream
import java.io.File
import java.nio.file.Files
import java.util.Properties
import java.util.logging.Logger
import org.bukkit.Bukkit
import org.bukkit.World

object WorldUtil {
    @JvmStatic fun getDefaultWorld(logger: Logger?): World? {
        val serverProps = File("server.properties")
        if (!serverProps.exists()) return Bukkit.getWorlds().firstOrNull()
        try {
            BufferedInputStream(Files.newInputStream(serverProps.toPath())).use { input ->
                val props = Properties().apply { load(input) }
                val worldName = props.getProperty("level-name")
                if (worldName != null) Bukkit.getWorld(worldName)?.let { return it }
            }
        } catch (e: Exception) {
            logger?.warning("读取 server.properties 失败: ${e.message}")
        }
        return Bukkit.getWorlds().firstOrNull()
    }
}
