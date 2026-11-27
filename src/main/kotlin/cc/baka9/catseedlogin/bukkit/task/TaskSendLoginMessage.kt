package cc.baka9.catseedlogin.bukkit.task

import cc.baka9.catseedlogin.bukkit.Cache
import cc.baka9.catseedlogin.bukkit.Config
import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayerHelper
import cc.baka9.catseedlogin.bukkit.`object`.TwoFactorLimiter
import cc.baka9.catseedlogin.bukkit.`object`.TwoFactorState
import cc.baka9.catseedlogin.common.i18n.MessageKey
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class TaskSendLoginMessage : Task() {
    override fun run() {
        if (!Cache.isLoaded) return
        TwoFactorLimiter.sweep(Config.Settings.TwoFactorAttemptWindow * 1000L)
        Bukkit.getOnlinePlayers().forEach { sendLoginMessage(it) }
    }
    private fun sendLoginMessage(player: Player) {
        try {
            val playerName = player.name
            if (LoginPlayerHelper.isLogin(playerName)) return
            if (TwoFactorState.isAwaitingTwoFactor(playerName)) { player.sendMessage(MessageKey.TWO_FACTOR_REQUIRED_REMINDER.get()); return }
            player.sendMessage(if (LoginPlayerHelper.isRegister(playerName)) Config.Language.LOGIN_REQUEST else Config.Language.REGISTER_REQUEST)
        } catch (e: Exception) { e.printStackTrace() }
    }
}
