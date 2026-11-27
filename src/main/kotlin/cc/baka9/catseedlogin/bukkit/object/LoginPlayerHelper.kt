package cc.baka9.catseedlogin.bukkit.`object`

import cc.baka9.catseedlogin.bukkit.Cache
import cc.baka9.catseedlogin.bukkit.CatScheduler
import cc.baka9.catseedlogin.bukkit.Config
import cc.baka9.catseedlogin.bukkit.PluginContext
import cc.baka9.catseedlogin.bukkit.event.CatSeedPlayerLoginEvent
import cc.baka9.catseedlogin.common.util.ValidationUtil
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.reflect.StructureModifier
import java.net.InetAddress
import java.util.Arrays
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.geysermc.floodgate.api.FloodgateApi

object LoginPlayerHelper {
    private val loginPlayers = ConcurrentHashMap<String, LoginPlayer>()
    private val playerExitTimes = ConcurrentHashMap<String, Long>()
    private const val MAX_STORED_IPS = 10

    @JvmStatic fun getList(): List<LoginPlayer> = ArrayList(loginPlayers.values)

    @JvmStatic fun add(lp: LoginPlayer?) {
        if (lp == null) return
        try { loginPlayers[lp.name] = lp }
        catch (e: Exception) { PluginContext.getLogger().severe("Failed to add LoginPlayer: ${e.message}") }
    }

    @JvmStatic fun remove(lp: LoginPlayer?) {
        if (lp == null) return
        try { loginPlayers.remove(lp.name) }
        catch (e: Exception) { PluginContext.getLogger().severe("Failed to remove LoginPlayer: ${e.message}") }
    }

    @JvmStatic fun remove(name: String?) {
        if (name == null) return
        try { loginPlayers.remove(name) }
        catch (e: Exception) { PluginContext.getLogger().severe("Failed to remove LoginPlayer by name: $name - ${e.message}") }
    }

    @JvmStatic fun isLogin(name: String): Boolean {
        if (TwoFactorState.isAwaitingTwoFactor(name)) return false
        return canBypassLogin(name) || loginPlayers.containsKey(name)
    }

    private fun canBypassLogin(name: String): Boolean =
        (Config.Settings.BedrockLoginBypass && isFloodgatePlayer(name)) ||
            (Config.Settings.LoginwiththesameIP && recordCurrentIP(name)) ||
            (Config.Settings.LoopbackLoginBypass && isLoopbackPlayer(name))

    @JvmStatic fun isRegister(name: String): Boolean =
        (Config.Settings.BedrockLoginBypass && isFloodgatePlayer(name)) || Cache.getIgnoreCase(name) != null

    @JvmStatic fun recordCurrentIP(name: String): Boolean = Bukkit.getPlayerExact(name)?.let { recordCurrentIP(it) } ?: false

    @JvmStatic fun recordCurrentIP(player: Player): Boolean {
        val currentIP = getPlayerIP(player) ?: return false
        val storedPlayer = Cache.getIgnoreCase(player.name) ?: return false
        val storedIPs = getStoredIPs(storedPlayer)
        val exitTime = playerExitTimes[player.name]
        if (ValidationUtil.isLoopbackAddress(currentIP)) return false
        return if (Config.Settings.IPTimeout == 0) storedIPs.contains(currentIP)
        else exitTime != null && storedIPs.contains(currentIP) &&
            System.currentTimeMillis() - exitTime <= Config.Settings.IPTimeout.toLong() * 60 * 1000
    }

    private fun getPlayerIP(player: Player?): String? = player?.address?.address?.hostAddress

    @JvmStatic fun recordPlayerExitTime(playerName: String?) {
        if (playerName == null) return
        if (Config.Settings.IPTimeout != 0 && isLogin(playerName)) {
            try { playerExitTimes[playerName] = System.currentTimeMillis() }
            catch (e: Exception) { PluginContext.getLogger().severe("Failed to record player exit time: $playerName - ${e.message}") }
        }
    }

    @JvmStatic fun onPlayerQuit(playerName: String) {
        recordPlayerExitTime(playerName)
        TwoFactorState.removePendingLogin(playerName)
    }

    @JvmStatic fun completeLogin(player: Player, lp: LoginPlayer) {
        add(lp)
        val loginEvent = CatSeedPlayerLoginEvent(player, lp.email, CatSeedPlayerLoginEvent.Result.SUCCESS)
        Bukkit.getServer().pluginManager.callEvent(loginEvent)
        player.sendMessage(Config.Language.LOGIN_SUCCESS)
        CatScheduler.updateInventory(player)
        recordCurrentIP(player, lp)
        if (Config.Settings.AfterLoginBack && Config.Settings.CanTpSpawnLocation) {
            Config.getOfflineLocation(player).ifPresent { location -> CatScheduler.teleport(player, location) }
        }
    }

    @JvmStatic fun getStoredIPs(lp: LoginPlayer?): List<String> {
        if (lp?.ips == null) return ArrayList()
        return try { lp.ips!!.split(';') } catch (_: Exception) { ArrayList() }
    }

    @JvmStatic fun isLoopbackPlayer(name: String): Boolean = Bukkit.getPlayerExact(name)?.let { isLoopbackPlayer(it) } ?: false
    @JvmStatic fun isLoopbackPlayer(player: Player): Boolean = getPlayerIP(player)?.let(ValidationUtil::isLoopbackAddress) ?: false
    @JvmStatic fun isFloodgatePlayer(name: String): Boolean = Bukkit.getPlayerExact(name)?.let { isFloodgatePlayer(it) } ?: false

    @JvmStatic fun isFloodgatePlayer(player: Player): Boolean = try {
        Bukkit.getPluginManager().getPlugin("floodgate") != null && FloodgateApi.getInstance().isFloodgatePlayer(player.uniqueId)
    } catch (_: Exception) { false }

    @JvmStatic fun getLastLoginTime(name: String): Long? = Cache.getIgnoreCase(name)?.lastAction

    @JvmStatic fun recordCurrentIP(player: Player, lp: LoginPlayer) {
        try {
            val currentIp = getPlayerIP(player) ?: return
            val ipsList = lp.getIpsList().distinct().toMutableList()
            ipsList.remove(currentIp)
            ipsList.add(currentIp)
            while (ipsList.size > MAX_STORED_IPS) ipsList.removeAt(0)
            lp.ips = ipsList.joinToString(";")
            savePlayerIPAsync(player, lp)
        } catch (e: Exception) {
            PluginContext.getLogger().warning("Failed to record IP for player: ${player.name} - ${e.message}")
        }
    }

    private fun savePlayerIPAsync(player: Player, lp: LoginPlayer) {
        CatScheduler.runTaskAsync { savePlayerIP(lp) }
    }

    private fun savePlayerIP(lp: LoginPlayer) {
        try { PluginContext.getSql().edit(lp); Cache.refresh(lp.name) }
        catch (e: Exception) { e.printStackTrace() }
    }

    @JvmStatic fun sendBlankInventoryPacket(player: Player) {
        if (!Config.Settings.EmptyBackpack) return
        try {
            val protocolManager = ProtocolLibrary.getProtocolManager()
            val inventoryPacket: PacketContainer = protocolManager.createPacket(PacketType.Play.Server.WINDOW_ITEMS)
            inventoryPacket.integers.write(0, 0)
            val blankInventory = Array(45) { ItemStack(Material.AIR) }
            val itemArrayModifier: StructureModifier<Array<ItemStack>> = inventoryPacket.itemArrayModifier
            if (itemArrayModifier.size() > 0) itemArrayModifier.write(0, blankInventory)
            else {
                val itemListModifier: StructureModifier<List<ItemStack>> = inventoryPacket.itemListModifier
                itemListModifier.write(0, Arrays.asList(*blankInventory))
            }
            protocolManager.sendServerPacket(player, inventoryPacket, false)
        } catch (e: Exception) { e.printStackTrace() }
    }
}
