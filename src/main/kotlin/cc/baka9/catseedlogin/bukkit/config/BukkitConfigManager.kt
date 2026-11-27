package cc.baka9.catseedlogin.bukkit.config

import cc.baka9.catseedlogin.bukkit.CatSeedLogin
import cc.baka9.catseedlogin.bukkit.util.WorldUtil
import cc.baka9.catseedlogin.common.config.BaseConfigManager
import cc.baka9.catseedlogin.common.config.ConfigConstants
import java.io.InputStream
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World

class BukkitConfigManager(private val plugin: CatSeedLogin) : BaseConfigManager() {
    init { initConfig(plugin.dataFolder, "config.yml") }
    override fun getResource(name: String): InputStream? = plugin.getResource(name)
    fun setSpawnLocation(location: Location) {
        val world = location.world ?: return
        mainConfig.set(ConfigConstants.Path.SPAWN_LOCATION, "${world.name}:${location.x}:${location.y}:${location.z}:${location.yaw}:${location.pitch}")
        saveConfig("config.yml")
    }
    fun getBukkitSpawnLocation(): Location? {
        val locStr = mainConfig.getString(ConfigConstants.Path.SPAWN_LOCATION)
        val defaultWorld = WorldUtil.getDefaultWorld(plugin.logger)
        if (locStr.isNullOrEmpty()) return fallbackSpawn(defaultWorld)?.also { setSpawnLocation(it) }
        val parts = locStr.split(":")
        if (parts.size < 6) return fallbackSpawn(defaultWorld)?.also { setSpawnLocation(it) }
        var world = Bukkit.getWorld(parts[0]) ?: defaultWorld
        if (world == null) world = Bukkit.getWorlds().firstOrNull() ?: return null
        return try {
            Location(world, parts[1].toDouble(), parts[2].toDouble(), parts[3].toDouble(), parts[4].toFloat(), parts[5].toFloat())
        } catch (_: NumberFormatException) {
            world.spawnLocation.also { setSpawnLocation(it) }
        }
    }
    private fun fallbackSpawn(defaultWorld: World?): Location? =
        defaultWorld?.spawnLocation ?: Bukkit.getWorlds().firstOrNull()?.spawnLocation
}
