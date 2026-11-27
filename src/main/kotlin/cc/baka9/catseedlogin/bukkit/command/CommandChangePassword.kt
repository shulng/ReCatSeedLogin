package cc.baka9.catseedlogin.bukkit.command

import cc.baka9.catseedlogin.bukkit.Cache
import cc.baka9.catseedlogin.bukkit.CatScheduler
import cc.baka9.catseedlogin.bukkit.Config
import cc.baka9.catseedlogin.bukkit.PluginContext
import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayer
import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayerHelper
import cc.baka9.catseedlogin.common.i18n.MessageKey
import cc.baka9.catseedlogin.common.util.Crypt
import cc.baka9.catseedlogin.common.util.PasswordHelper
import cc.baka9.catseedlogin.common.util.ValidationUtil
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandChangePassword : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<String>): Boolean {
        if (args.size != 3 || sender !is Player) return false
        val player = sender
        val name = player.name
        if (Config.Settings.BedrockLoginBypass && LoginPlayerHelper.isFloodgatePlayer(player)) return true
        val lp = Cache.getIgnoreCase(name)
        if (lp == null) { sender.sendMessage(Config.Language.CHANGEPASSWORD_NOREGISTER); return true }
        if (!LoginPlayerHelper.isLogin(name)) { sender.sendMessage(Config.Language.CHANGEPASSWORD_NOLOGIN); return true }
        if (!Crypt.match(name, args[0], lp.password.trim())) { sender.sendMessage(Config.Language.CHANGEPASSWORD_OLDPASSWORD_INCORRECT); return true }
        if (args[1] != args[2]) { sender.sendMessage(Config.Language.CHANGEPASSWORD_PASSWORD_CONFIRM_FAIL); return true }
        if (ValidationUtil.isPasswordTooSimple(args[1])) { sender.sendMessage(Config.Language.COMMON_PASSWORD_SO_SIMPLE); return true }
        if (!Cache.isLoaded) return true
        sender.sendMessage(MessageKey.CHANGING_PASSWORD.get())
        changePasswordAsync(sender, player, lp, args[1])
        return true
    }
    private fun changePasswordAsync(sender: CommandSender, player: Player, lp: LoginPlayer, newPwd: String) {
        CatScheduler.runTaskAsync { executePasswordChange(sender, player, lp, newPwd) }
    }
    private fun executePasswordChange(sender: CommandSender, player: Player, lp: LoginPlayer, newPwd: String) {
        try {
            val copy = PasswordHelper.updatePassword(lp, newPwd)
            PluginContext.getSql().edit(copy)
            Cache.refresh(copy.name)
            LoginPlayerHelper.remove(lp)
            CatScheduler.runTask { notifyChangeSuccess(sender, player) }
        } catch (e: Exception) { e.printStackTrace(); sender.sendMessage(MessageKey.INTERNAL_ERROR.get()) }
    }
    private fun notifyChangeSuccess(sender: CommandSender, player: Player) {
        val online = Bukkit.getPlayer(player.uniqueId)
        if (online == null || !online.isOnline) return
        online.sendMessage(Config.Language.CHANGEPASSWORD_SUCCESS)
        Config.setOfflineLocation(online)
        if (!Config.Settings.CanTpSpawnLocation) return
        CatScheduler.teleport(online, Config.Settings.SpawnLocation)
        if (PluginContext.isLoadProtocolLib()) LoginPlayerHelper.sendBlankInventoryPacket(online)
    }
}
