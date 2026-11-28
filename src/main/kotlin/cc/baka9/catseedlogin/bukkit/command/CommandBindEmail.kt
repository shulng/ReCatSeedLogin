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
import cc.baka9.catseedlogin.common.util.ValidationUtil
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandBindEmail : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, s: String, args: Array<String>): Boolean {
        if (args.isEmpty() || sender !is Player) return false
        val name = sender.name
        if (!canBindEmail(sender, name)) return true
        when (args[0].lowercase()) {
            "set" -> handleSet(sender, name, args)
            "verify" -> handleVerify(sender, name, args)
        }
        return true
    }
    private fun canBindEmail(sender: CommandSender, name: String): Boolean {
        val player = sender as Player
        if (Config.Settings.BedrockLoginBypass && LoginPlayerHelper.isFloodgatePlayer(player)) return false
        if (Cache.getIgnoreCase(name) == null) { sender.sendMessage(MessageKey.NOT_REGISTERED.get()); return false }
        if (!LoginPlayerHelper.isLogin(name)) { sender.sendMessage(MessageKey.NOT_LOGGED_IN.get()); return false }
        if (!Config.EmailVerify.Enable) { sender.sendMessage(MessageKey.RESETPASSWORD_EMAIL_DISABLE.get()); return false }
        return true
    }
    private fun handleSet(sender: CommandSender, name: String, args: Array<String>) {
        if (args.size <= 1) return
        val lp = Cache.getIgnoreCase(name) ?: return
        if (lp.email != null && ValidationUtil.isValidEmail(lp.email)) { sender.sendMessage(MessageKey.EMAIL_ALREADY_BOUND.get()); return }
        val mail = args[1]
        if (!ValidationUtil.isValidEmail(mail)) { sender.sendMessage(MessageKey.EMAIL_FORMAT_INVALID.get()); return }
        try {
            val existingCode = EmailCode.getByName(name, EmailCode.Type.Bind)
            if (existingCode.isPresent && existingCode.get().email == mail) { sender.sendMessage(MessageKey.EMAIL_CODE_ALREADY_SENT.get(mail)); return }
        } catch (e: Exception) { e.printStackTrace() }
        val bindEmail = try { EmailCode.create(name, mail, 1000L * 60 * 5, EmailCode.Type.Bind) }
        catch (e: Exception) { sender.sendMessage(MessageKey.INTERNAL_ERROR.get()); e.printStackTrace(); return }
        sender.sendMessage(MessageKey.SENDING_EMAIL_CODE.get())
        sendEmailCode(sender, name, mail, bindEmail)
    }
    private fun handleVerify(sender: CommandSender, name: String, args: Array<String>) {
        if (args.size <= 1) return
        val lp = Cache.getIgnoreCase(name) ?: return
        if (lp.email != null && ValidationUtil.isValidEmail(lp.email)) { sender.sendMessage(MessageKey.EMAIL_ALREADY_BOUND.get()); return }
        val emailOptional = EmailCode.getByName(name, EmailCode.Type.Bind)
        if (!emailOptional.isPresent) { sender.sendMessage(MessageKey.NO_PENDING_EMAIL_CODE.get()); return }
        val bindEmail = emailOptional.get()
        if (bindEmail.code != args[1]) { sender.sendMessage(MessageKey.VERIFICATION_CODE_INCORRECT.get()); return }
        sender.sendMessage(MessageKey.BINDING_EMAIL.get())
        bindEmail(sender, lp, bindEmail)
    }
    private fun sendEmailCode(sender: CommandSender, name: String, mail: String, bindEmail: EmailCode) {
        CatScheduler.runTaskAsync {
            try {
                if (EmailSender.sendEmail(mail, MessageKey.EMAIL_SUBJECT_BIND_EMAIL.get(), buildBindEmailContent(name, bindEmail))) notifyBindEmailSent(sender, mail)
                else notifyBindEmailFailed(sender)
            } catch (e: Exception) { notifyBindEmailFailed(sender); e.printStackTrace() }
        }
    }
    private fun buildBindEmailContent(name: String, bindEmail: EmailCode): String =
        MessageKey.EMAIL_BIND_EMAIL_CONTENT.get(bindEmail.code, name, bindEmail.durability / (1000 * 60))
    private fun notifyBindEmailSent(sender: CommandSender, mail: String) {
        CatScheduler.runTask { sender.sendMessage(MessageKey.EMAIL_SENT_CHECK_INBOX.get(mail)); sender.sendMessage(MessageKey.CHECK_SPAM_FOLDER.get()) }
    }
    private fun notifyBindEmailFailed(sender: CommandSender) { CatScheduler.runTask { sender.sendMessage(MessageKey.EMAIL_SEND_FAILED.get()) } }
    private fun bindEmail(sender: CommandSender, lp: LoginPlayer, bindEmail: EmailCode) {
        CatScheduler.runTaskAsync { executeBindEmail(sender, lp, bindEmail) }
    }
    private fun executeBindEmail(sender: CommandSender, lp: LoginPlayer, bindEmail: EmailCode) {
        try {
            lp.email = bindEmail.email
            PluginContext.getSql().edit(lp)
            Cache.refresh(lp.name)
            CatScheduler.runTask { notifyBindSuccess(sender, bindEmail) }
        } catch (e: Exception) {
            e.printStackTrace()
            CatScheduler.runTask { sender.sendMessage(MessageKey.INTERNAL_ERROR.get()) }
        }
    }
    private fun notifyBindSuccess(sender: CommandSender, bindEmail: EmailCode) {
        val syncPlayer = Bukkit.getPlayer((sender as Player).uniqueId)
        if (syncPlayer == null || !syncPlayer.isOnline) return
        syncPlayer.sendMessage(MessageKey.EMAIL_BOUND_SUCCESS.get(bindEmail.email))
        EmailCode.removeByName(syncPlayer.name, EmailCode.Type.Bind)
    }
}
