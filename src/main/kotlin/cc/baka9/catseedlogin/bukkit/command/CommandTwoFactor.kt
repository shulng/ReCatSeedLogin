package cc.baka9.catseedlogin.bukkit.command

import cc.baka9.catseedlogin.bukkit.Cache
import cc.baka9.catseedlogin.bukkit.CatScheduler
import cc.baka9.catseedlogin.bukkit.Config
import cc.baka9.catseedlogin.bukkit.PluginContext
import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayer
import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayerHelper
import cc.baka9.catseedlogin.bukkit.`object`.TotpRecoveryCode
import cc.baka9.catseedlogin.bukkit.`object`.TwoFactorLimiter
import cc.baka9.catseedlogin.bukkit.`object`.TwoFactorState
import cc.baka9.catseedlogin.bukkit.util.RecoveryCodeGenerator
import cc.baka9.catseedlogin.bukkit.util.TwoFactorMessageUtil
import cc.baka9.catseedlogin.common.i18n.MessageKey
import cc.baka9.catseedlogin.common.util.SecretCodec
import cc.baka9.catseedlogin.common.util.TabCompleteUtil
import cc.baka9.catseedlogin.common.util.TotpUtil
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class CommandTwoFactor : CommandExecutor, TabCompleter {
    private val recoveryInFlight = ConcurrentHashMap.newKeySet<UUID>()
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) { sender.sendMessage(MessageKey.TWO_FACTOR_PLAYER_ONLY.get()); return true }
        if (args.isEmpty()) { sender.sendMessage(MessageKey.TWO_FACTOR_USAGE.get()); return true }
        return when (args[0].lowercase()) {
            "enable" -> enable(sender)
            "verify" -> verify(sender, args)
            "recover" -> recover(sender, args)
            "disable" -> disable(sender, args)
            else -> { sender.sendMessage(MessageKey.TWO_FACTOR_USAGE.get()); true }
        }
    }
    private fun enable(player: Player): Boolean {
        if (!Config.Settings.TwoFactorEnabled) { player.sendMessage(MessageKey.TWO_FACTOR_DISABLED_GLOBAL.get()); return true }
        val name = player.name
        if (!LoginPlayerHelper.isLogin(name)) { player.sendMessage(MessageKey.TWO_FACTOR_NEED_LOGIN.get()); return true }
        val lp = Cache.getIgnoreCase(name)
        if (lp == null) { player.sendMessage(MessageKey.TWO_FACTOR_NEED_LOGIN.get()); return true }
        if (lp.totpEnabled) { player.sendMessage(MessageKey.TWO_FACTOR_ALREADY_ENABLED.get()); return true }
        val secret = TotpUtil.generateSecret()
        TwoFactorState.putPendingBind(name, secret)
        sendSecret(player, secret)
        return true
    }
    private fun verify(player: Player, args: Array<String>): Boolean {
        if (args.size < 2) { player.sendMessage(MessageKey.TWO_FACTOR_USAGE.get()); return true }
        val name = player.name
        val secret = TwoFactorState.getPendingBind(name)
        if (secret != null) return verifyBind(player, name, secret, args[1])
        val pending = TwoFactorState.getPendingLogin(name)
        if (pending != null) return verifyLogin(player, pending, args[1])
        player.sendMessage(MessageKey.TWO_FACTOR_NO_PENDING.get())
        return true
    }
    private fun verifyBind(player: Player, name: String, secret: String, code: String): Boolean {
        if (!TotpUtil.verify(secret, code, System.currentTimeMillis(), digits(), period(), 1)) { handleVerifyFailure(player); return true }
        TwoFactorLimiter.recordSuccess(player.uniqueId)
        TwoFactorState.removePendingBind(name)
        enableTwoFactor(player, name, secret)
        return true
    }
    private fun verifyLogin(player: Player, pending: TwoFactorState.PendingLogin, code: String): Boolean {
        val lp = pending.loginPlayer
        if (!player.isOnline || TwoFactorState.getPendingLogin(player.name) !== pending) {
            TwoFactorState.removePendingLogin(player.name)
            return true
        }
        val timeoutSeconds = Config.Settings.TwoFactorPendingTimeout
        if (timeoutSeconds > 0 && System.currentTimeMillis() - pending.timestamp > timeoutSeconds * 1000L) {
            TwoFactorState.removePendingLogin(player.name)
            player.sendMessage(MessageKey.TWO_FACTOR_NO_PENDING.get())
            return true
        }
        val secret = SecretCodec.decrypt(lp.totpSecret)
        if (secret == null) { TwoFactorState.removePendingLogin(lp.name); player.sendMessage(MessageKey.TWO_FACTOR_SECRET_BROKEN.get()); return true }
        if (!TotpUtil.verify(secret, code, System.currentTimeMillis(), digits(), period(), 1)) { handleVerifyFailure(player); return true }
        TwoFactorLimiter.recordSuccess(player.uniqueId)
        TwoFactorState.removePendingLogin(lp.name)
        LoginPlayerHelper.completeLogin(player, lp)
        return true
    }
    private fun recover(player: Player, args: Array<String>): Boolean {
        if (args.size < 2) { player.sendMessage(MessageKey.TWO_FACTOR_USAGE.get()); return true }
        val name = player.name
        val pending = TwoFactorState.getPendingLogin(name)
        if (pending == null) { player.sendMessage(MessageKey.TWO_FACTOR_RECOVER_NOT_PENDING.get()); return true }
        if (!recoveryInFlight.add(player.uniqueId)) {
            player.sendMessage(MessageKey.TWO_FACTOR_RECOVER_INVALID.get())
            return true
        }
        CatScheduler.runTaskAsync {
            try {
                val matched = PluginContext.getSql().consumeRecoveryCode(name, args[1])
                CatScheduler.runTask {
                    recoveryInFlight.remove(player.uniqueId)
                    if (!player.isOnline || TwoFactorState.getPendingLogin(player.name) !== pending) return@runTask
                    if (matched == null) handleVerifyFailure(player) else finishRecover(player, pending, matched)
                }
            } catch (exception: Exception) {
                recoveryInFlight.remove(player.uniqueId)
                PluginContext.getLogger().warning("恢复码校验失败: ${exception.message}")
            }
        }
        return true
    }
    private fun finishRecover(player: Player, pending: TwoFactorState.PendingLogin, matched: TotpRecoveryCode) {
        val name = pending.loginPlayer.name
        if (!player.isOnline || TwoFactorState.getPendingLogin(player.name) !== pending) return
        if (!PluginContext.getSql().recoverTwoFactorState(name)) return
        Cache.refresh(name)
        TwoFactorLimiter.recordSuccess(player.uniqueId)
        TwoFactorState.removePendingLogin(name)
        TwoFactorState.setRebindRequired(name)
        LoginPlayerHelper.completeLogin(player, pending.loginPlayer)
        player.sendMessage(MessageKey.TWO_FACTOR_RECOVER_SUCCESS.get())
    }
    private fun handleVerifyFailure(player: Player) {
        val exceeded = TwoFactorLimiter.recordFailure(player.uniqueId, Config.Settings.TwoFactorMaxAttempts, Config.Settings.TwoFactorAttemptWindow * 1000L)
        if (!exceeded) { player.sendMessage(MessageKey.TWO_FACTOR_INVALID_CODE.get()); return }
        player.sendMessage(MessageKey.TWO_FACTOR_MAX_ATTEMPTS.get())
        player.kickPlayer(MessageKey.TWO_FACTOR_MAX_ATTEMPTS.get())
    }
    private fun disable(player: Player, args: Array<String>): Boolean {
        if (args.size < 2) { player.sendMessage(MessageKey.TWO_FACTOR_USAGE.get()); return true }
        val name = player.name
        if (!LoginPlayerHelper.isLogin(name)) { player.sendMessage(MessageKey.TWO_FACTOR_NEED_LOGIN.get()); return true }
        val lp = Cache.getIgnoreCase(name)
        if (lp == null || !lp.totpEnabled) { player.sendMessage(MessageKey.TWO_FACTOR_NOT_ENABLED.get()); return true }
        val secret = SecretCodec.decrypt(lp.totpSecret)
        if (secret == null) { player.sendMessage(MessageKey.TWO_FACTOR_SECRET_BROKEN.get()); return true }
        if (!TotpUtil.verify(secret, args[1], System.currentTimeMillis(), digits(), period(), 1)) { handleVerifyFailure(player); return true }
        TwoFactorLimiter.recordSuccess(player.uniqueId)
        if (!PluginContext.getSql().disableTwoFactorState(name)) return true
        Cache.refresh(name)
        player.sendMessage(MessageKey.TWO_FACTOR_DISABLED.get())
        return true
    }
    private fun enableTwoFactor(player: Player, name: String, secret: String) {
        val encrypted = try { SecretCodec.encrypt(secret) }
        catch (_: Exception) { player.sendMessage(MessageKey.TWO_FACTOR_SECRET_BROKEN.get()); return }
        CatScheduler.runTaskAsync {
            val generated = RecoveryCodeGenerator.generate(name, Config.Settings.TwoFactorRecoveryCodeCount)
            if (!PluginContext.getSql().saveTwoFactorState(name, encrypted, generated.getEntries())) return@runTaskAsync
            Cache.refresh(name)
            TwoFactorState.removeRebindRequired(name)
            CatScheduler.runTask { player.sendMessage(MessageKey.TWO_FACTOR_ENABLED.get()) }
            CatScheduler.runTask { showRecoveryCodes(player, generated.getRawCodes()) }
        }
    }
    private fun sendSecret(player: Player, secret: String) {
        val uri = TotpUtil.buildOtpAuthUri(Config.Settings.TwoFactorIssuer, player.name, secret)
        TwoFactorMessageUtil.sendText(player, MessageKey.TWO_FACTOR_SECRET_SENT.get())
        TwoFactorMessageUtil.send(player, listOf(TwoFactorMessageUtil.MessageLine(MessageKey.TWO_FACTOR_SECRET_LABEL.get(secret), secret, MessageKey.TWO_FACTOR_SECRET_HOVER.get())))
        TwoFactorMessageUtil.send(player, listOf(TwoFactorMessageUtil.MessageLine(MessageKey.TWO_FACTOR_URI_LABEL.get(uri), uri, MessageKey.TWO_FACTOR_URI_HOVER.get())))
        TwoFactorMessageUtil.sendText(player, MessageKey.TWO_FACTOR_VERIFY_PROMPT.get())
    }
    private fun showRecoveryCodes(player: Player, codes: List<String>) {
        player.sendMessage(MessageKey.TWO_FACTOR_RECOVERY_WARN.get())
        if (LoginPlayerHelper.isFloodgatePlayer(player)) player.sendMessage(MessageKey.TWO_FACTOR_RECOVERY_BEDROCK.get())
        val all = codes.joinToString("\n")
        TwoFactorMessageUtil.send(player, listOf(TwoFactorMessageUtil.MessageLine(MessageKey.TWO_FACTOR_RECOVERY_COPY_ALL.get(), all, MessageKey.TWO_FACTOR_RECOVERY_COPY_ALL_HOVER.get())))
        codes.forEachIndexed { index, code ->
            CatScheduler.runTaskLater({ TwoFactorMessageUtil.send(player, listOf(TwoFactorMessageUtil.MessageLine(code, code, MessageKey.TWO_FACTOR_RECOVERY_ITEM_HOVER.get()))) }, (index + 1) * 3L)
        }
    }
    private fun digits() = Config.Settings.TwoFactorDigits.takeIf { it > 0 } ?: TotpUtil.DEFAULT_DIGITS
    private fun period() = Config.Settings.TwoFactorPeriod.takeIf { it > 0 } ?: TotpUtil.DEFAULT_PERIOD
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> =
        if (args.size == 1) TabCompleteUtil.filter(SUB_COMMANDS, args[0]) else emptyList()

    companion object { private val SUB_COMMANDS = listOf("enable", "verify", "recover", "disable") }
}
