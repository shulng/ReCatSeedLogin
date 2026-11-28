package cc.baka9.catseedlogin.bukkit.command

import cc.baka9.catseedlogin.bukkit.Cache
import cc.baka9.catseedlogin.bukkit.CatScheduler
import cc.baka9.catseedlogin.bukkit.Config
import cc.baka9.catseedlogin.bukkit.PluginContext
import cc.baka9.catseedlogin.bukkit.`object`.EmailCode
import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayer
import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayerHelper
import cc.baka9.catseedlogin.bukkit.util.EmailSender
import cc.baka9.catseedlogin.common.i18n.MessageKey
import cc.baka9.catseedlogin.common.util.PasswordHelper
import cc.baka9.catseedlogin.common.util.ValidationUtil
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandResetPassword : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, s: String, args: Array<String>): Boolean {
        if (args.isEmpty() || sender !is Player) return false
        val player = sender
        val name = player.name
        if (Config.Settings.BedrockLoginBypass && LoginPlayerHelper.isFloodgatePlayer(player)) return true
        val lp = Cache.getIgnoreCase(name)
        if (lp == null) { sender.sendMessage(Config.Language.RESETPASSWORD_NOREGISTER); return true }
        if (!Config.EmailVerify.Enable) { sender.sendMessage(Config.Language.RESETPASSWORD_EMAIL_DISABLE); return true }
        if (args[0].equals("forget", true)) return handleForget(sender, name, lp)
        if (args[0].equals("re", true) && args.size > 2) return handleReset(player, lp, args[1], args[2])
        return true
    }
    private fun handleForget(sender: CommandSender, name: String, lp: LoginPlayer): Boolean {
        val email = lp.email
        if (email == null) { sender.sendMessage(Config.Language.RESETPASSWORD_EMAIL_NO_SET); return true }
        try {
            val optional = EmailCode.getByName(name, EmailCode.Type.ResetPassword)
            if (optional.isPresent) { sender.sendMessage(Config.Language.RESETPASSWORD_EMAIL_REPEAT_SEND_MESSAGE.replace("{email}", optional.get().email)); return true }
        } catch (e: Exception) { e.printStackTrace() }
        val emailCode = try { EmailCode.create(name, email, EMAIL_CODE_DURATION, EmailCode.Type.ResetPassword) }
        catch (e: Exception) { sender.sendMessage(MessageKey.INTERNAL_ERROR.get()); e.printStackTrace(); return true }
        sender.sendMessage(Config.Language.RESETPASSWORD_EMAIL_SENDING_MESSAGE.replace("{email}", email))
        sendResetEmailAsync(sender, name, emailCode)
        return true
    }
    private fun sendResetEmailAsync(sender: CommandSender, name: String, emailCode: EmailCode) {
        CatScheduler.runTaskAsync {
            try {
                if (EmailSender.sendEmail(emailCode.email, MessageKey.EMAIL_SUBJECT_RESET_PASSWORD.get(), buildResetEmailContent(name, emailCode))) notifyEmailSent(sender, emailCode.email)
                else notifyEmailFailed(sender)
            } catch (e: Exception) { notifyEmailFailed(sender); e.printStackTrace() }
        }
    }
    private fun buildResetEmailContent(name: String, emailCode: EmailCode): String =
        MessageKey.EMAIL_RESET_PASSWORD_CONTENT.get(emailCode.code, name, emailCode.durability / (1000 * 60))
    private fun notifyEmailSent(sender: CommandSender, email: String) {
        Bukkit.getScheduler().runTask(PluginContext.getPlugin(), Runnable { sender.sendMessage(Config.Language.RESETPASSWORD_EMAIL_SENT_MESSAGE.replace("{email}", email)) })
    }
    private fun notifyEmailFailed(sender: CommandSender) {
        Bukkit.getScheduler().runTask(PluginContext.getPlugin(), Runnable { sender.sendMessage(Config.Language.RESETPASSWORD_EMAIL_WARN) })
    }
    private fun handleReset(player: Player, lp: LoginPlayer, code: String, pwd: String): Boolean {
        val sender: CommandSender = player
        if (lp.email == null) { sender.sendMessage(Config.Language.RESETPASSWORD_EMAIL_NO_SET); return true }
        try {
            val optional = EmailCode.getByName(lp.name, EmailCode.Type.ResetPassword)
            if (!optional.isPresent) { sender.sendMessage(Config.Language.RESETPASSWORD_FAIL); return true }
            if (optional.get().code != code) { sender.sendMessage(Config.Language.RESETPASSWORD_EMAILCODE_INCORRECT); return true }
        } catch (e: Exception) { e.printStackTrace(); sender.sendMessage(Config.Language.RESETPASSWORD_FAIL); return true }
        if (ValidationUtil.isPasswordTooSimple(pwd)) { sender.sendMessage(Config.Language.COMMON_PASSWORD_SO_SIMPLE); return true }
        sender.sendMessage(MessageKey.RESETTING_PASSWORD.get())
        processPasswordResetAsync(player, lp, pwd)
        return true
    }
    private fun processPasswordResetAsync(player: Player, lp: LoginPlayer, pwd: String) {
        val name = lp.name
        CatScheduler.runTaskAsync { executePasswordReset(name, lp, pwd, player) }
    }
    private fun executePasswordReset(name: String, lp: LoginPlayer, pwd: String, sender: CommandSender) {
        try {
            val copy = PasswordHelper.updatePassword(lp, pwd)
            PluginContext.getSql().edit(copy)
            Cache.refresh(name)
            LoginPlayerHelper.remove(lp)
            EmailCode.removeByName(name, EmailCode.Type.ResetPassword)
            CatScheduler.runTask { notifyResetSuccess(name, Bukkit.getPlayer(name)) }
        } catch (e: Exception) {
            CatScheduler.runTask { sender.sendMessage(MessageKey.DATABASE_ERROR.get()) }
            e.printStackTrace()
        }
    }
    private fun notifyResetSuccess(name: String, player: Player?) {
        val p = Bukkit.getPlayer(name)
        if (p == null || !p.isOnline) return
        if (Config.Settings.CanTpSpawnLocation) CatScheduler.teleport(p, Config.Settings.SpawnLocation)
        p.sendMessage(Config.Language.RESETPASSWORD_SUCCESS)
        if (PluginContext.isLoadProtocolLib()) LoginPlayerHelper.sendBlankInventoryPacket(p)
    }
    companion object { private const val EMAIL_CODE_DURATION = 1000L * 60 * 5 }
}
