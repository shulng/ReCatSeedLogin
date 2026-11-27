package cc.baka9.catseedlogin.bukkit

import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayerHelper
import cc.baka9.catseedlogin.bukkit.event.CatSeedPlayerLoginEvent
import cc.baka9.catseedlogin.bukkit.event.CatSeedPlayerRegisterEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class BlindingListeners : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!Config.Settings.BlindingBeforeLogin) return
        val player = event.player
        if (LoginPlayerHelper.isLogin(player.name)) return
        addBlindness(player)
    }

    @EventHandler
    fun onPlayerLogin(event: CatSeedPlayerLoginEvent) {
        if (event.result != CatSeedPlayerLoginEvent.Result.SUCCESS) return
        removeBlindness(event.player)
    }

    @EventHandler
    fun onPlayerRegister(event: CatSeedPlayerRegisterEvent) {
        removeBlindness(event.player)
    }

    @Suppress("DEPRECATION")
    private fun addBlindness(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, BLINDNESS_DURATION, 0), true)
    }

    private fun removeBlindness(player: Player?) {
        player?.removePotionEffect(PotionEffectType.BLINDNESS)
    }

    companion object {
        private const val BLINDNESS_DURATION = 20 * 60 * 60 * 24
    }
}
