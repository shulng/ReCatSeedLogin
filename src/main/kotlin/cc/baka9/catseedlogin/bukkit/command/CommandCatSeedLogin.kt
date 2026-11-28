package cc.baka9.catseedlogin.bukkit.command

import cc.baka9.catseedlogin.bukkit.Cache
import cc.baka9.catseedlogin.bukkit.CatScheduler
import cc.baka9.catseedlogin.bukkit.Communication
import cc.baka9.catseedlogin.bukkit.Config
import cc.baka9.catseedlogin.bukkit.PluginContext
import cc.baka9.catseedlogin.bukkit.database.MySQL
import cc.baka9.catseedlogin.bukkit.database.SQLite
import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayer
import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayerHelper
import cc.baka9.catseedlogin.bukkit.`object`.TwoFactorState
import cc.baka9.catseedlogin.common.i18n.MessageKey
import cc.baka9.catseedlogin.common.util.PasswordHelper
import cc.baka9.catseedlogin.common.util.TabCompleteUtil
import cc.baka9.catseedlogin.common.util.ValidationUtil
import java.util.LinkedHashSet
import java.util.regex.Pattern
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class CommandCatSeedLogin : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, lable: String, args: Array<String>): Boolean =
        reload(sender, args) || setPwd(sender, args) || delPlayer(sender, args) || twoFactorDisable(sender, args) ||
            loopbackLoginBypass(sender, args) || beforeLoginAllowChat(sender, args) || blindingBeforeLogin(sender, args) ||
            setIpCountLimit(sender, args) || limitChineseID(sender, args) || bedrockLoginBypass(sender, args) ||
            LoginwiththesameIP(sender, args) || setIdLength(sender, args) || beforeLoginNoDamage(sender, args) ||
            setReenterInterval(sender, args) || afterLoginBack(sender, args) || setSpawnLocation(sender, args) ||
            commandWhiteListInfo(sender, args) || commandWhiteListAdd(sender, args) || commandWhiteListDel(sender, args) ||
            canTpSpawnLocation(sender, args) || autoKick(sender, args) || setIpRegCountLimit(sender, args) ||
            deathStateQuitRecordLocation(sender, args)

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        if (!sender.hasPermission(PERMISSION)) return emptyList()
        if (args.size == 1) return TabCompleteUtil.filter(SUB_COMMANDS, args[0])
        if (args.size == 2) {
            when (args[0].lowercase()) {
                "delplayer", "setpwd", "2fadisable" -> return TabCompleteUtil.filter(registeredAndOnlinePlayerNames(), args[1])
                "commandwhitelistdel" -> return TabCompleteUtil.filter(commandWhiteListRegexes(), args[1])
            }
        }
        return emptyList()
    }

    private class BoolSetting(val getter: () -> Boolean, val setter: (Boolean) -> Unit, val label: String)
    private fun toggle(sender: CommandSender, args: Array<String>, key: String, setting: BoolSetting): Boolean {
        if (args.isEmpty() || !args[0].equals(key, true)) return false
        try {
            setting.setter(!setting.getter())
            Config.Settings.save()
            sender.sendMessage(if (setting.getter()) MessageKey.ADMIN_TOGGLE_ON.get(setting.label) else MessageKey.ADMIN_TOGGLE_OFF.get(setting.label))
        } catch (e: Exception) { sender.sendMessage(MessageKey.ADMIN_SET_FAILED.get(e.message)) }
        return true
    }
    private fun deathStateQuitRecordLocation(s: CommandSender, a: Array<String>) = toggle(s, a, "deathStateQuitRecordLocation", BoolSetting({ Config.Settings.DeathStateQuitRecordLocation }, { Config.Settings.DeathStateQuitRecordLocation = it }, "死亡状态退出游戏记录退出位置"))
    private fun canTpSpawnLocation(s: CommandSender, a: Array<String>) = toggle(s, a, "canTpSpawnLocation", BoolSetting({ Config.Settings.CanTpSpawnLocation }, { Config.Settings.CanTpSpawnLocation = it }, "登录之前强制在登陆地点"))
    private fun afterLoginBack(s: CommandSender, a: Array<String>) = toggle(s, a, "afterLoginBack", BoolSetting({ Config.Settings.AfterLoginBack }, { Config.Settings.AfterLoginBack = it }, "登陆之后返回下线地点"))
    private fun beforeLoginNoDamage(s: CommandSender, a: Array<String>) = toggle(s, a, "beforeLoginNoDamage", BoolSetting({ Config.Settings.BeforeLoginNoDamage }, { Config.Settings.BeforeLoginNoDamage = it }, "登陆之前不受到伤害"))
    private fun limitChineseID(s: CommandSender, a: Array<String>) = toggle(s, a, "limitChineseID", BoolSetting({ Config.Settings.LimitChineseID }, { Config.Settings.LimitChineseID = it }, "限制中文游戏名"))
    private fun bedrockLoginBypass(s: CommandSender, a: Array<String>) = toggle(s, a, "bedrockLoginBypass", BoolSetting({ Config.Settings.BedrockLoginBypass }, { Config.Settings.BedrockLoginBypass = it }, "基岩版玩家登录跳过"))
    private fun LoginwiththesameIP(s: CommandSender, a: Array<String>) = toggle(s, a, "LoginwiththesameIP", BoolSetting({ Config.Settings.LoginwiththesameIP }, { Config.Settings.LoginwiththesameIP = it }, "同IP玩家登录跳过"))
    private fun loopbackLoginBypass(s: CommandSender, a: Array<String>) = toggle(s, a, "loopbackLoginBypass", BoolSetting({ Config.Settings.LoopbackLoginBypass }, { Config.Settings.LoopbackLoginBypass = it }, "本地回环地址登录跳过"))
    private fun beforeLoginAllowChat(s: CommandSender, a: Array<String>) = toggle(s, a, "beforeLoginAllowChat", BoolSetting({ Config.Settings.BeforeLoginAllowChat }, { Config.Settings.BeforeLoginAllowChat = it }, "登陆之前允许发消息"))
    private fun blindingBeforeLogin(s: CommandSender, a: Array<String>) = toggle(s, a, "blindingBeforeLogin", BoolSetting({ Config.Settings.BlindingBeforeLogin }, { Config.Settings.BlindingBeforeLogin = it }, "登陆之前失明效果"))

    private fun autoKick(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size < 2 || !args[0].equals("setAutoKick", true)) return false
        try { Config.Settings.AutoKick = args[1].toInt(); Config.Settings.save(); sender.sendMessage(if (Config.Settings.AutoKick > 0) MessageKey.ADMIN_AUTO_KICK_SET.get(Config.Settings.AutoKick) else MessageKey.ADMIN_AUTO_KICK_DISABLED.get()) }
        catch (_: NumberFormatException) { sender.sendMessage(MessageKey.ADMIN_ENTER_NUMBER.get()) }
        return true
    }
    private fun setReenterInterval(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size < 2 || !args[0].equals("setReenterInterval", true)) return false
        try { Config.Settings.ReenterInterval = args[1].toLong(); Config.Settings.save(); sender.sendMessage(MessageKey.ADMIN_REENTER_INTERVAL_SET.get(Config.Settings.ReenterInterval)) }
        catch (_: NumberFormatException) { sender.sendMessage(MessageKey.ADMIN_ENTER_NUMBER.get()) }
        return true
    }
    private fun setIdLength(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size < 3 || !args[0].equals("setIdLength", true)) return false
        try { Config.Settings.MinLengthID = args[1].toInt(); Config.Settings.MaxLengthID = args[2].toInt(); Config.Settings.save(); sender.sendMessage(MessageKey.ADMIN_ID_LENGTH_SET.get(Config.Settings.MinLengthID, Config.Settings.MaxLengthID)) }
        catch (_: NumberFormatException) { sender.sendMessage(MessageKey.ADMIN_ENTER_NUMBER.get()) }
        return true
    }
    private fun setIpCountLimit(sender: CommandSender, args: Array<String>) = setInt(sender, args, "setIpCountLimit", { Config.Settings.IpCountLimit = it; MessageKey.ADMIN_IP_LOGIN_LIMIT_SET.get(it) })
    private fun setIpRegCountLimit(sender: CommandSender, args: Array<String>) = setInt(sender, args, "setIpRegCountLimit", { Config.Settings.IpRegisterCountLimit = it; MessageKey.ADMIN_IP_REG_LIMIT_SET.get(it) })
    private fun setInt(sender: CommandSender, args: Array<String>, key: String, action: (Int) -> String): Boolean {
        if (args.size < 2 || !args[0].equals(key, true)) return false
        try { val value = args[1].toInt(); val message = action(value); Config.Settings.save(); sender.sendMessage(message) }
        catch (_: NumberFormatException) { sender.sendMessage(MessageKey.ADMIN_ENTER_NUMBER.get()) }
        return true
    }

    private fun commandWhiteListInfo(sender: CommandSender, args: Array<String>): Boolean {
        if (args.isEmpty() || !args[0].equals("commandWhiteListInfo", true)) return false
        sender.sendMessage(MessageKey.ADMIN_COMMAND_WHITELIST_INFO.get()); Config.Settings.CommandWhiteList.forEach { sender.sendMessage(it.toString()) }; return true
    }
    private fun commandWhiteListAdd(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size < 2 || !args[0].equals("commandWhiteListAdd", true)) return false
        val regex = args.drop(1).joinToString(" ")
        val pattern = try { Pattern.compile(regex) } catch (_: java.util.regex.PatternSyntaxException) {
            sender.sendMessage(MessageKey.ADMIN_SET_FAILED.get("非法正则: $regex")); return true
        }
        if (containsRegex(regex)) sender.sendMessage(MessageKey.ADMIN_COMMAND_WHITELIST_ALREADY_EXISTS.get(regex))
        else { Config.Settings.CommandWhiteList = Config.Settings.CommandWhiteList + pattern; Config.Settings.save(); sender.sendMessage(MessageKey.ADMIN_COMMAND_WHITELIST_ADDED.get(regex)) }
        return true
    }
    private fun commandWhiteListDel(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size < 2 || !args[0].equals("commandWhiteListDel", true)) return false
        val regex = args.drop(1).joinToString(" ")
        if (containsRegex(regex)) { Config.Settings.CommandWhiteList = Config.Settings.CommandWhiteList.filterNot { it.toString() == regex }; Config.Settings.save(); sender.sendMessage(MessageKey.ADMIN_COMMAND_WHITELIST_REMOVED.get(regex)) }
        else sender.sendMessage(MessageKey.ADMIN_COMMAND_WHITELIST_NOT_EXISTS.get(regex))
        return true
    }
    private fun containsRegex(regex: String) = Config.Settings.CommandWhiteList.any { it.toString() == regex }

    private fun setSpawnLocation(sender: CommandSender, args: Array<String>): Boolean {
        if (args.isEmpty() || !args[0].equals("setSpawnLocation", true)) return false
        if (sender !is Player) { sender.sendMessage(MessageKey.CANNOT_USE_FROM_CONSOLE.get()); return true }
        Config.Settings.SpawnLocation = sender.location; Config.Settings.save(); sender.sendMessage(MessageKey.SPAWN_LOCATION_SET_MSG.get()); return true
    }
    private fun reload(sender: CommandSender, args: Array<String>): Boolean {
        if (args.isEmpty() || !args[0].equals("reload", true)) return false
        Config.reload()
        try { PluginContext.getSql().closeConnection() } catch (e: Exception) { PluginContext.getLogger().warning("§c关闭旧数据库连接时出错"); e.printStackTrace() }
        PluginContext.setSql(if (Config.MySQL.Enable) MySQL(PluginContext.getPlugin()) else SQLite(PluginContext.getPlugin()))
        try { PluginContext.getSql().init(); Cache.refreshAllSync() } catch (e: Exception) { PluginContext.getLogger().warning("§c加载数据库时出错"); e.printStackTrace() }
        if (Config.BungeeCord.Enable) try { Communication.socketServerRestartAsync() } catch (e: Exception) { PluginContext.getLogger().warning("§c重启通信服务时出错"); e.printStackTrace() }
        else try { Communication.socketServerStopAsync() } catch (e: Exception) { PluginContext.getLogger().warning("§c停止通信服务时出错"); e.printStackTrace() }
        sender.sendMessage(MessageKey.CONFIG_RELOADED_MSG.get()); return true
    }
    private fun delPlayer(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size < 2 || !args[0].equals("delplayer", true)) return false
        val lp = Cache.getIgnoreCase(args[1])
        if (lp == null) { sender.sendMessage(MessageKey.ACCOUNT_NOT_EXISTS.get(args[1])); return true }
        delPlayerAsync(sender, lp); return true
    }
    private fun delPlayerAsync(sender: CommandSender, lp: LoginPlayer) {
        CatScheduler.runTaskAsync {
            try {
                PluginContext.getSql().del(lp.name); Cache.refresh(lp.name); LoginPlayerHelper.remove(lp)
                CatScheduler.runTask { sender.sendMessage(MessageKey.ACCOUNT_DELETED.get(lp.name)); kickPlayerIfOnline(lp.name) }
            } catch (e: Exception) {
                CatScheduler.runTask { sender.sendMessage(MessageKey.DATABASE_ERROR.get()) }
                e.printStackTrace()
            }
        }
    }
    private fun kickPlayerIfOnline(name: String) {
        CatScheduler.runTask { Bukkit.getPlayerExact(name)?.takeIf { it.isOnline }?.kickPlayer(MessageKey.ACCOUNT_DELETED_KICK.get()) }
    }
    private fun twoFactorDisable(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size < 2 || !args[0].equals("2fadisable", true)) return false
        val name = args[1]; val lp = Cache.getIgnoreCase(name)
        if (lp == null) { sender.sendMessage(MessageKey.ACCOUNT_NOT_EXISTS.get(name)); return true }
        if (!lp.totpEnabled) { sender.sendMessage(MessageKey.ADMIN_2FA_NOT_ENABLED.get(name)); return true }
        if (!PluginContext.getSql().disableTwoFactorState(name)) { sender.sendMessage(MessageKey.DATABASE_ERROR.get()); return true }
        Cache.refresh(name); TwoFactorState.removePendingLogin(name); sender.sendMessage(MessageKey.ADMIN_2FA_DISABLED.get(name))
        Bukkit.getPlayerExact(name)?.takeIf { it.isOnline }?.sendMessage(MessageKey.TWO_FACTOR_DISABLED_BY_ADMIN.get())
        return true
    }
    private fun setPwd(sender: CommandSender, args: Array<String>): Boolean {
        if (args.size < 3 || !args[0].equals("setpwd", true)) return false
        val name = args[1]; val pwd = args[2]
        if (ValidationUtil.isPasswordTooSimple(pwd)) { sender.sendMessage(MessageKey.PASSWORD_TOO_SIMPLE_MSG.get()); return true }
        sender.sendMessage(MessageKey.SETTING_PASSWORD.get()); CatScheduler.runTaskAsync { setPwdLookup(sender, name, pwd) }; return true
    }
    private fun setPwdLookup(sender: CommandSender, name: String, pwd: String) {
        val lp = Cache.getIgnoreCase(name)
        if (lp == null) setPwdRegisterNew(sender, name, pwd) else setPwdUpdateExisting(sender, lp, pwd)
    }
    private fun setPwdRegisterNew(sender: CommandSender, name: String, pwd: String) {
        try {
            val lp = PasswordHelper.registerNewPlayer(name, pwd); PluginContext.getSql().add(lp); Cache.refresh(lp.name)
            CatScheduler.runTask { sender.sendMessage(MessageKey.ACCOUNT_NOT_EXISTS_REGISTERED.get()) }
        } catch (e: Exception) {
            CatScheduler.runTask { sender.sendMessage(MessageKey.DATABASE_ERROR.get()) }; e.printStackTrace()
        }
    }
    private fun setPwdUpdateExisting(sender: CommandSender, lp: LoginPlayer, pwd: String) {
        try {
            val copy = PasswordHelper.updatePassword(lp, pwd); PluginContext.getSql().edit(copy); Cache.refresh(copy.name); LoginPlayerHelper.remove(lp)
            CatScheduler.runTask { sender.sendMessage(MessageKey.PASSWORD_SET_MSG.get(lp.name)); notifyPlayerPasswordChanged(lp) }
        } catch (e: Exception) {
            CatScheduler.runTask { sender.sendMessage(MessageKey.DATABASE_ERROR.get()) }; e.printStackTrace()
        }
    }
    private fun notifyPlayerPasswordChanged(lp: LoginPlayer) {
        CatScheduler.runTask {
            val p = Bukkit.getPlayer(lp.name) ?: return@runTask
            if (!p.isOnline) return@runTask
            p.sendMessage(MessageKey.PASSWORD_RESET_BY_ADMIN.get())
            if (!Config.Settings.CanTpSpawnLocation) return@runTask
            CatScheduler.teleport(p, Config.Settings.SpawnLocation)
            if (PluginContext.isLoadProtocolLib()) LoginPlayerHelper.sendBlankInventoryPacket(p)
        }
    }

    companion object {
        const val PERMISSION = "catseedlogin.command.catseedlogin"
        @JvmField val SUB_COMMANDS = listOf("reload", "setPwd", "delPlayer", "setIpCountLimit", "setIpRegCountLimit", "setIdLength", "setReenterInterval", "setAutoKick", "setSpawnLocation", "limitChineseID", "bedrockLoginBypass", "LoginwiththesameIP", "beforeLoginNoDamage", "afterLoginBack", "canTpSpawnLocation", "deathStateQuitRecordLocation", "commandWhiteListInfo", "commandWhiteListAdd", "commandWhiteListDel", "loopbackLoginBypass", "beforeLoginAllowChat", "blindingBeforeLogin", "2faDisable")
        private fun registeredAndOnlinePlayerNames(): List<String> {
            val names = LinkedHashSet<String>()
            Bukkit.getOnlinePlayers().forEach { names.add(it.name) }
            if (Cache.isLoaded) Cache.getAllLoginPlayer().forEach { lp -> if (lp.name.isNotEmpty()) names.add(lp.name) }
            return ArrayList(names)
        }
        private fun commandWhiteListRegexes(): List<String> = Config.Settings.CommandWhiteList?.map(Pattern::toString) ?: emptyList()
    }
}
