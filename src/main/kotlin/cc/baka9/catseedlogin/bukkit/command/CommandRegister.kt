package cc.baka9.catseedlogin.bukkit.command

import cc.baka9.catseedlogin.bukkit.Cache
import cc.baka9.catseedlogin.bukkit.CatScheduler
import cc.baka9.catseedlogin.bukkit.Config
import cc.baka9.catseedlogin.bukkit.PluginContext
import cc.baka9.catseedlogin.bukkit.event.CatSeedPlayerRegisterEvent
import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayer
import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayerHelper
import cc.baka9.catseedlogin.common.i18n.MessageKey
import cc.baka9.catseedlogin.common.util.PasswordHelper
import cc.baka9.catseedlogin.common.util.ValidationUtil
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandRegister : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<String>): Boolean {
        if (args.size != 2 || sender !is Player) return false
        val player = sender
        val name = sender.name
        if (!canRegister(player, name)) return true
        if (args[0] != args[1]) { sender.sendMessage(Config.Language.REGISTER_PASSWORD_CONFIRM_FAIL); return true }
        if (ValidationUtil.isPasswordTooSimple(args[0])) { sender.sendMessage(Config.Language.COMMON_PASSWORD_SO_SIMPLE); return true }
        if (!Cache.isLoaded) return true
        sender.sendMessage(MessageKey.REGISTERING.get())
        registerPlayerAsync(player, name, args[0])
        return true
    }
    private fun canRegister(player: Player, name: String): Boolean {
        if (Config.Settings.BedrockLoginBypass && LoginPlayerHelper.isFloodgatePlayer(player)) return false
        if (LoginPlayerHelper.isLogin(name)) { player.sendMessage(Config.Language.REGISTER_AFTER_LOGIN_ALREADY); return false }
        if (LoginPlayerHelper.isRegister(name)) { player.sendMessage(Config.Language.REGISTER_BEFORE_LOGIN_ALREADY); return false }
        return true
    }
    private fun registerPlayerAsync(player: Player, name: String, password: String) {
        val address = player.address?.address
        if (address == null) { player.sendMessage(MessageKey.INTERNAL_ERROR.get()); return }
        val currentIp = address.hostAddress
        val isLoopback = address.isLoopbackAddress
        CatScheduler.runTaskAsync {
            try { processRegistration(player, name, password, currentIp, isLoopback) }
            catch (e: Exception) { e.printStackTrace(); player.sendMessage(MessageKey.INTERNAL_ERROR.get()) }
        }
    }
    @Throws(Exception::class)
    private fun processRegistration(player: Player, name: String, password: String, currentIp: String, isLoopback: Boolean) {
        val loginPlayersByIp = PluginContext.getSql().getLikeByIp(currentIp)
        if (!isLoopback && loginPlayersByIp.size >= Config.Settings.IpRegisterCountLimit) {
            val accounts = loginPlayersByIp.joinToString(", ") { it.name }
            player.sendMessage(Config.Language.REGISTER_MORE.replace("{count}", loginPlayersByIp.size.toString()).replace("{accounts}", accounts))
            return
        }
        val lp = PasswordHelper.registerNewPlayer(name, password)
        PluginContext.getSql().add(lp)
        Cache.refresh(lp.name)
        LoginPlayerHelper.add(lp)
        CatScheduler.runTask {
            val registeredPlayer = Bukkit.getPlayer(name) ?: return@runTask
            Bukkit.getServer().pluginManager.callEvent(CatSeedPlayerRegisterEvent(registeredPlayer))
        }
        player.sendMessage(Config.Language.REGISTER_SUCCESS)
        CatScheduler.updateInventory(player)
        LoginPlayerHelper.recordCurrentIP(player, lp)
    }
}
