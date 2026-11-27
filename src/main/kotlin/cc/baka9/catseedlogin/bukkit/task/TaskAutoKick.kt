package cc.baka9.catseedlogin.bukkit.task

import cc.baka9.catseedlogin.bukkit.Cache
import cc.baka9.catseedlogin.bukkit.Config
import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayerHelper
import cc.baka9.catseedlogin.bukkit.`object`.TwoFactorState
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class TaskAutoKick : Task() {
    private val playerJoinTime = ConcurrentHashMap<String, Long>()
    override fun run() {
        if (!Cache.isLoaded || Config.Settings.AutoKick < 1) return
        val autoKickMs = Config.Settings.AutoKick * 1000L
        val now = System.currentTimeMillis()
        Bukkit.getOnlinePlayers().forEach { checkAndKickPlayer(it, now, autoKickMs) }
    }
    fun removePlayer(playerName: String) { playerJoinTime.remove(playerName) }
    private fun checkAndKickPlayer(player: Player, now: Long, autoKickMs: Long) {
        val playerName = player.name
        try {
            if (LoginPlayerHelper.isLogin(playerName)) { playerJoinTime.remove(playerName); return }
            val pending = TwoFactorState.getPendingLogin(playerName)
            if (pending != null) {
                val pendingSeconds = Config.Settings.TwoFactorPendingTimeout
                val pendingMs = if (pendingSeconds > 0) pendingSeconds * 1000L else autoKickMs
                if (now - pending.timestamp <= pendingMs) return
                kickPlayer(player, if (pendingSeconds > 0) pendingSeconds else Config.Settings.AutoKick); return
            }
            checkAndKickTimeoutPlayer(player, now, autoKickMs)
        } catch (e: Exception) { playerJoinTime.remove(playerName); e.printStackTrace() }
    }
    private fun checkAndKickTimeoutPlayer(player: Player, now: Long, autoKickMs: Long) {
        val playerName = player.name
        playerJoinTime.putIfAbsent(playerName, now)
        val joinTime = playerJoinTime[playerName] ?: return
        if (now - joinTime <= autoKickMs) return
        if (!player.isOnline) { playerJoinTime.remove(playerName); return }
        kickPlayer(player, Config.Settings.AutoKick)
    }
    private fun kickPlayer(player: Player, seconds: Int) { player.kickPlayer(Config.Language.AUTO_KICK.replace("{time}", seconds.toString())) }
}
