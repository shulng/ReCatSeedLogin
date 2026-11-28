package cc.baka9.catseedlogin.bukkit

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.time.Duration
import org.bukkit.Location
import org.bukkit.entity.Player
import space.arim.morepaperlib.MorePaperLib
import space.arim.morepaperlib.scheduling.ScheduledTask

object CatScheduler {
    private lateinit var morePaperLib: MorePaperLib
    private var folia = false
    private var teleportAsync: Method? = null

    @JvmStatic
    fun init(mpl: MorePaperLib) {
        morePaperLib = mpl
        folia = mpl.scheduling().isUsingFolia()
        teleportAsync = initTeleportAsync()
    }

    private fun initTeleportAsync(): Method? {
        if (!folia) return null
        return try {
            Player::class.java.getMethod("teleportAsync", Location::class.java)
        } catch (e: NoSuchMethodException) {
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    fun teleport(player: Player, location: Location?) {
        if (location?.world == null) return
        if (!folia) {
            player.teleport(location)
            return
        }
        morePaperLib.scheduling().entitySpecificScheduler(player).run(Runnable {
            try {
                teleportAsync?.invoke(player, location)
                Unit
            } catch (e: IllegalAccessException) {
                throw RuntimeException(e)
            } catch (e: InvocationTargetException) {
                throw RuntimeException(e)
            }
        }, null)
    }

    @JvmStatic
    fun updateInventory(player: Player) {
        morePaperLib.scheduling().entitySpecificScheduler(player).run(Runnable { player.updateInventory() }, null)
    }

    @JvmStatic
    fun runTaskAsync(runnable: Runnable): ScheduledTask = morePaperLib.scheduling().asyncScheduler().run(runnable)

    @JvmStatic
    fun runTaskTimer(runnable: Runnable, delay: Long, period: Long): ScheduledTask =
        morePaperLib.scheduling().globalRegionalScheduler().runAtFixedRate(runnable, if (delay == 0L) 1 else delay, period)

    @JvmStatic
    fun runTask(runnable: Runnable): ScheduledTask = morePaperLib.scheduling().globalRegionalScheduler().run(runnable)

    @JvmStatic
    fun runTask(player: Player, runnable: Runnable): ScheduledTask? =
        morePaperLib.scheduling().entitySpecificScheduler(player).run(runnable, null)

    @JvmStatic
    fun runTaskLater(runnable: Runnable, delay: Long): ScheduledTask =
        morePaperLib.scheduling().globalRegionalScheduler().runDelayed(runnable, delay)

    @JvmStatic
    fun runTaskLaterAsync(runnable: Runnable, delay: Long): ScheduledTask =
        morePaperLib.scheduling().asyncScheduler().runDelayed(runnable, Duration.ofMillis(delay * 50))

    @JvmStatic
    fun runTaskTimerAsync(runnable: Runnable, delay: Long, period: Long): ScheduledTask =
        morePaperLib.scheduling().asyncScheduler().runAtFixedRate(
            runnable,
            Duration.ofMillis(delay * 50),
            Duration.ofMillis(period * 50)
        )
}
