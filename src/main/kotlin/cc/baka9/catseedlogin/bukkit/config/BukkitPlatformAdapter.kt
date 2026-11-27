package cc.baka9.catseedlogin.bukkit.config

import cc.baka9.catseedlogin.bukkit.CatScheduler
import cc.baka9.catseedlogin.bukkit.CatSeedLogin
import cc.baka9.catseedlogin.common.api.PlatformAdapter
import cc.baka9.catseedlogin.common.i18n.I18n
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class BukkitPlatformAdapter(private val plugin: CatSeedLogin, private val i18n: I18n) : PlatformAdapter {
    override fun getName() = "Bukkit"
    override fun logInfo(message: String) { plugin.logger.info(message) }
    override fun logWarn(message: String) { plugin.logger.warning(message) }
    override fun logError(message: String) { plugin.logger.severe(message) }
    override fun logError(message: String, throwable: Throwable) { plugin.logger.severe(message); throwable.printStackTrace() }
    override fun runAsync(task: Runnable) { CatScheduler.runTaskAsync(task) }
    override fun runSync(task: Runnable) { CatScheduler.runTask(task) }
    override fun runAsyncLater(task: Runnable, delayTicks: Long) { CatScheduler.runTaskLaterAsync(task, delayTicks) }
    override fun runSyncLater(task: Runnable, delayTicks: Long) { CatScheduler.runTaskLater(task, delayTicks) }
    override fun runAsyncTimer(task: Runnable, delayTicks: Long, periodTicks: Long) { CatScheduler.runTaskTimerAsync(task, delayTicks, periodTicks) }
    override fun runSyncTimer(task: Runnable, delayTicks: Long, periodTicks: Long) { CatScheduler.runTaskTimer(task, delayTicks, periodTicks) }
    override fun getI18n() = i18n
    override fun getPlatformPlayer(name: String): Any? = Bukkit.getPlayerExact(name)
    override fun isPlayerOnline(name: String): Boolean = Bukkit.getPlayerExact(name)?.isOnline == true
    override fun kickPlayer(name: String, reason: String) { Bukkit.getPlayerExact(name)?.kickPlayer(reason) }
    override fun sendMessage(playerName: String, message: String) { Bukkit.getPlayerExact(playerName)?.sendMessage(message) }
    override fun broadcast(message: String) { Bukkit.broadcastMessage(message) }
}
