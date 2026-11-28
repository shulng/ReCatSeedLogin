package cc.baka9.catseedlogin.bukkit

import cc.baka9.catseedlogin.bukkit.command.CommandBindEmail
import cc.baka9.catseedlogin.bukkit.command.CommandCatSeedLogin
import cc.baka9.catseedlogin.bukkit.command.CommandChangePassword
import cc.baka9.catseedlogin.bukkit.command.CommandLogin
import cc.baka9.catseedlogin.bukkit.command.CommandRegister
import cc.baka9.catseedlogin.bukkit.command.CommandResetPassword
import cc.baka9.catseedlogin.bukkit.command.CommandTwoFactor
import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager
import cc.baka9.catseedlogin.bukkit.config.BukkitPlatformAdapter
import cc.baka9.catseedlogin.bukkit.database.MySQL
import cc.baka9.catseedlogin.bukkit.database.SQL
import cc.baka9.catseedlogin.bukkit.database.SQLite
import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayerHelper
import cc.baka9.catseedlogin.bukkit.`object`.TwoFactorState
import cc.baka9.catseedlogin.bukkit.task.Task
import cc.baka9.catseedlogin.bukkit.util.TwoFactorMessageUtil
import cc.baka9.catseedlogin.common.i18n.I18n
import cc.baka9.catseedlogin.common.util.SecretCodec
import cc.baka9.catseedlogin.common.util.TabCompleteUtil
import java.util.Collections
import org.bukkit.Bukkit
import org.bukkit.command.PluginCommand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import space.arim.morepaperlib.MorePaperLib

class CatSeedLogin : JavaPlugin(), Listener {
    companion object {
        @Volatile
        @JvmStatic
        lateinit var instance: CatSeedLogin

        @Volatile
        @JvmStatic
        lateinit var sql: SQL

        @Volatile
        @JvmStatic
        var loadProtocolLib: Boolean = false

        @Volatile
        @JvmStatic
        lateinit var morePaperLib: MorePaperLib

        private const val LEGACY_BLINDING_PLUGIN = "CatSeedLoginBlinding"
    }

    private lateinit var configManager: BukkitConfigManager
    private lateinit var platformAdapter: BukkitPlatformAdapter

    override fun onEnable() {
        instance = this
        morePaperLib = MorePaperLib(this)
        CatScheduler.init(morePaperLib)
        server.pluginManager.registerEvents(this, this)

        configManager = BukkitConfigManager(this)
        platformAdapter = BukkitPlatformAdapter(this, configManager.getI18n())
        try {
            configManager.reload()
            Config.load()
        } catch (e: Exception) {
            e.printStackTrace()
            server.logger.warning("加载配置文件时出错，请检查你的配置文件。")
            server.pluginManager.disablePlugin(this)
            return
        }

        initTwoFactorSupport()
        val database: SQL = if (configManager.isMySQL()) MySQL(this) else SQLite(this)
        sql = database
        try {
            database.init()
            Cache.refreshAll()
        } catch (e: Exception) {
            logger.warning("§c加载数据库时出错")
            e.printStackTrace()
            server.pluginManager.disablePlugin(this)
            return
        }

        server.pluginManager.registerEvents(Listeners(), this)
        checkBlindingPluginConflict()
        server.pluginManager.registerEvents(BlindingListeners(), this)

        if (configManager.isEmptyBackpack()) {
            try {
                Class.forName("com.comphenix.protocol.ProtocolLib")
                ProtocolLibListeners.enable()
                loadProtocolLib = true
            } catch (_: ClassNotFoundException) {
                logger.warning("服务器没有装载ProtocolLib插件，这将无法使用登录前隐藏背包")
            }
        }

        PluginContext.init(this, database, loadProtocolLib)
        if (configManager.isEnable()) Communication.socketServerStartAsync()
        if (Bukkit.getPluginManager().getPlugin("floodgate") != null && configManager.isBedrockLoginBypass()) {
            logger.info("检测到floodgate，基岩版兼容已装载")
        }

        registerCommands()
        Task.runAll()
    }

    private fun initTwoFactorSupport() {
        TwoFactorMessageUtil.probe()
        try {
            SecretCodec.init(dataFolder)
        } catch (e: Exception) {
            logger.warning("初始化两步验证密钥文件失败，两步验证将不可用: ${e.message}")
        }
    }

    private fun checkBlindingPluginConflict() {
        if (server.pluginManager.getPlugin(LEGACY_BLINDING_PLUGIN) == null) return
        logger.warning("检测到 $LEGACY_BLINDING_PLUGIN 插件，与内置的登录前失明功能冲突！")
        logger.warning("请删除 $LEGACY_BLINDING_PLUGIN 插件，改用 settings.before-login.blinding 控制该功能")
    }

    private fun registerCommands() {
        registerLoginCommand()
        registerRegisterCommand()
        registerChangePasswordCommand()
        registerBindEmailCommand()
        registerResetPasswordCommand()
        registerTwoFactorCommand()
        registerCatSeedLoginCommand()
    }

    private fun registerTwoFactorCommand() {
        val command = server.getPluginCommand("2fa") ?: return
        val executor = CommandTwoFactor()
        command.setExecutor(executor)
        command.tabCompleter = executor
    }

    private fun registerLoginCommand() {
        val command = server.getPluginCommand("login") ?: return
        command.setExecutor(CommandLogin())
        command.setTabCompleter { _, _, _, args ->
            if (args.size == 1) TabCompleteUtil.filter(Collections.singletonList("密码"), args[0])
            else emptyList()
        }
    }

    private fun registerRegisterCommand() {
        val command = server.getPluginCommand("register") ?: return
        command.setExecutor(CommandRegister())
        command.setTabCompleter { _, _, _, args ->
            if (args.size == 1) TabCompleteUtil.filter(Collections.singletonList("密码 重复密码"), args[0])
            else emptyList()
        }
    }

    private fun registerChangePasswordCommand() {
        val command = server.getPluginCommand("changepassword") ?: return
        command.setExecutor(CommandChangePassword())
        command.setTabCompleter { _, _, _, args ->
            if (args.size == 1) TabCompleteUtil.filter(Collections.singletonList("旧密码 新密码 重复新密码"), args[0])
            else emptyList()
        }
    }

    private fun registerBindEmailCommand() {
        val command = server.getPluginCommand("bindemail") ?: return
        command.setExecutor(CommandBindEmail())
        command.setTabCompleter { _, _, _, args ->
            when {
                args.size == 1 -> TabCompleteUtil.filter(listOf("set", "verify"), args[0])
                args.size == 2 && args[0].equals("set", ignoreCase = true) ->
                    TabCompleteUtil.filter(Collections.singletonList("需要绑定的邮箱"), args[1])
                args.size == 2 && args[0].equals("verify", ignoreCase = true) ->
                    TabCompleteUtil.filter(Collections.singletonList("邮箱获取的验证码"), args[1])
                else -> emptyList()
            }
        }
    }

    private fun registerResetPasswordCommand() {
        val command = server.getPluginCommand("resetpassword") ?: return
        command.setExecutor(CommandResetPassword())
        command.setTabCompleter { _, _, _, args ->
            when {
                args.size == 1 -> TabCompleteUtil.filter(listOf("forget", "re"), args[0])
                args.size == 2 && args[0].equals("re", ignoreCase = true) ->
                    TabCompleteUtil.filter(Collections.singletonList("验证码"), args[1])
                args.size == 3 && args[0].equals("re", ignoreCase = true) ->
                    TabCompleteUtil.filter(Collections.singletonList("新密码"), args[2])
                else -> emptyList()
            }
        }
    }

    private fun registerCatSeedLoginCommand() {
        val command = server.getPluginCommand("catseedlogin") ?: return
        val executor = CommandCatSeedLogin()
        command.setExecutor(executor)
        command.tabCompleter = executor
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        LoginPlayerHelper.onPlayerQuit(event.player.name)
    }

    override fun onDisable() {
        Task.cancelAll()
        TwoFactorState.clear()
        Bukkit.getOnlinePlayers().forEach { player ->
            if (LoginPlayerHelper.isLogin(player.name) && (!player.isDead || configManager.isDeathStateQuitRecordLocation())) {
                Config.setOfflineLocationSync(player)
            }
        }

        try {
            sql.closeConnection()
        } catch (e: Exception) {
            logger.warning("关闭数据库连接时出错")
            e.printStackTrace()
        }
        Communication.socketServerStop()
        super.onDisable()
    }

    fun runTaskAsync(runnable: Runnable?) {
        if (runnable != null) CatScheduler.runTaskAsync(runnable)
    }

    fun getConfigManager(): BukkitConfigManager = configManager

    fun getPlatformAdapter(): BukkitPlatformAdapter = platformAdapter

    fun getI18n(): I18n = configManager.getI18n()
}
