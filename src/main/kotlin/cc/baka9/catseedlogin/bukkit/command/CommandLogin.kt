package cc.baka9.catseedlogin.bukkit.command

import cc.baka9.catseedlogin.bukkit.Cache
import cc.baka9.catseedlogin.bukkit.CatScheduler
import cc.baka9.catseedlogin.bukkit.Config
import cc.baka9.catseedlogin.bukkit.PluginContext
import cc.baka9.catseedlogin.bukkit.event.CatSeedPlayerLoginEvent
import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayer
import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayerHelper
import cc.baka9.catseedlogin.bukkit.`object`.TwoFactorState
import cc.baka9.catseedlogin.common.i18n.MessageKey
import cc.baka9.catseedlogin.common.util.Crypt
import cc.baka9.catseedlogin.common.util.PasswordHelper
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandLogin : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<String>): Boolean {
        if (args.isEmpty() || sender !is Player) return false
        val player = sender
        val name = player.name
        if (Config.Settings.BedrockLoginBypass && LoginPlayerHelper.isFloodgatePlayer(player)) return true
        if (LoginPlayerHelper.isLogin(name)) { sender.sendMessage(Config.Language.LOGIN_REPEAT); return true }
        val lp = Cache.getIgnoreCase(name)
        if (lp == null) { sender.sendMessage(Config.Language.LOGIN_NOREGISTER); return true }
        if (!Crypt.match(name, args[0], lp.password.trim())) { handleLoginFail(sender, player, lp); return true }
        if (requiresTwoFactor(lp)) {
            TwoFactorState.setPendingLogin(lp)
            player.sendMessage(MessageKey.TWO_FACTOR_REQUIRED.get())
            return true
        }
        LoginPlayerHelper.completeLogin(player, lp)
        if (!Crypt.isArgon2(lp.password.trim())) upgradeToArgon2(lp, args[0])
        return true
    }

    private fun requiresTwoFactor(lp: LoginPlayer) = Config.Settings.TwoFactorEnabled && lp.totpEnabled
    private fun upgradeToArgon2(lp: LoginPlayer, rawPassword: String) {
        CatScheduler.runTaskAsync {
            try {
                val copy = PasswordHelper.updatePassword(lp, rawPassword)
                PluginContext.getSql().edit(copy)
                Cache.refresh(copy.name)
            } catch (_: Exception) {
                PluginContext.getLogger().warning("Failed to upgrade password hash to Argon2id for ${lp.name}")
            }
        }
    }
    private fun handleLoginFail(sender: CommandSender, player: Player, lp: LoginPlayer) {
        sender.sendMessage(Config.Language.LOGIN_FAIL)
        Bukkit.getServer().pluginManager.callEvent(CatSeedPlayerLoginEvent(player, lp.email, CatSeedPlayerLoginEvent.Result.FAIL))
        if (Config.EmailVerify.Enable) sender.sendMessage(Config.Language.LOGIN_FAIL_IF_FORGET)
    }
}
