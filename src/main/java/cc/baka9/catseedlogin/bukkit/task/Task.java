package cc.baka9.catseedlogin.bukkit.task;

import cc.baka9.catseedlogin.bukkit.BukkitContext;
import cc.baka9.catseedlogin.bukkit.scheduler.CatScheduler;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import space.arim.morepaperlib.scheduling.ScheduledTask;

/** 定时任务基类：统一登记与取消插件内的周期任务。 */
public abstract class Task implements Runnable {

  private static final List<ScheduledTask> scheduledTasks = new CopyOnWriteArrayList<>();
  private static TaskAutoKick taskAutoKick;
  private static TaskSendLoginMessage taskSendLoginMessage;

  protected Task() {}

  public static TaskAutoKick getTaskAutoKick() {
    return taskAutoKick == null ? (taskAutoKick = new TaskAutoKick()) : taskAutoKick;
  }

  public static TaskSendLoginMessage getTaskSendLoginMessage() {
    return taskSendLoginMessage == null
        ? (taskSendLoginMessage = new TaskSendLoginMessage())
        : taskSendLoginMessage;
  }

  public static void runAll() {
    runTaskTimer(getTaskSendLoginMessage(), 20 * 5);
    runTaskTimer(getTaskAutoKick(), 20 * 5);
  }

  public static void cancelAll() {
    scheduledTasks.forEach(ScheduledTask::cancel);
    scheduledTasks.clear();
  }

  public static void runTaskTimer(Runnable runnable, long period) {
    try {
      scheduledTasks.add(CatScheduler.runTaskTimer(runnable, 0, period));
    } catch (Exception e) {
      BukkitContext.getLogger().severe(e.getMessage());
    }
  }
}
