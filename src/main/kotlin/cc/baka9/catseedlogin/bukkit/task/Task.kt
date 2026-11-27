package cc.baka9.catseedlogin.bukkit.task

import cc.baka9.catseedlogin.bukkit.CatScheduler
import cc.baka9.catseedlogin.bukkit.CatSeedLogin
import java.util.concurrent.CopyOnWriteArrayList
import space.arim.morepaperlib.scheduling.ScheduledTask

abstract class Task : Runnable {
    companion object {
        private val scheduledTasks = CopyOnWriteArrayList<ScheduledTask>()
        private var taskAutoKick: TaskAutoKick? = null
        private var taskSendLoginMessage: TaskSendLoginMessage? = null

        @JvmStatic
        fun getTaskAutoKick(): TaskAutoKick = taskAutoKick ?: TaskAutoKick().also { taskAutoKick = it }

        @JvmStatic
        fun getTaskSendLoginMessage(): TaskSendLoginMessage =
            taskSendLoginMessage ?: TaskSendLoginMessage().also { taskSendLoginMessage = it }

        @JvmStatic
        fun runAll() {
            runTaskTimer(getTaskSendLoginMessage(), 20 * 5L)
            runTaskTimer(getTaskAutoKick(), 20 * 5L)
        }

        @JvmStatic
        fun cancelAll() {
            scheduledTasks.forEach { it.cancel() }
            scheduledTasks.clear()
        }

        @JvmStatic
        fun runTaskTimer(runnable: Runnable, delay: Long) {
            try {
                scheduledTasks.add(CatScheduler.runTaskTimer(runnable, 0, delay))
            } catch (e: Exception) {
                CatSeedLogin.instance.logger.severe(e.message)
            }
        }
    }
}
