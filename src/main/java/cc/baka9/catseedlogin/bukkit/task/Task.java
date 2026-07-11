package cc.baka9.catseedlogin.bukkit.task;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import cc.baka9.catseedlogin.bukkit.CatScheduler;
import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import space.arim.morepaperlib.scheduling.ScheduledTask;

public abstract class Task implements Runnable {
    private static final List<ScheduledTask> scheduledTasks = new CopyOnWriteArrayList<>();
    private static volatile TaskAutoKick taskAutoKick;
    private static volatile TaskSendLoginMessage taskSendLoginMessage;

    protected Task() {}

    public static TaskAutoKick getTaskAutoKick() {
        TaskAutoKick result = taskAutoKick;
        if (result == null) {
            synchronized (Task.class) {
                result = taskAutoKick;
                if (result == null) {
                    taskAutoKick = result = new TaskAutoKick();
                }
            }
        }
        return result;
    }

    public static TaskSendLoginMessage getTaskSendLoginMessage() {
        TaskSendLoginMessage result = taskSendLoginMessage;
        if (result == null) {
            synchronized (Task.class) {
                result = taskSendLoginMessage;
                if (result == null) {
                    taskSendLoginMessage = result = new TaskSendLoginMessage();
                }
            }
        }
        return result;
    }

    public static void runAll() {
        runTaskTimer(getTaskSendLoginMessage(), 20 * 5);
        runTaskTimer(getTaskAutoKick(), 20 * 5);
    }

    public static void cancelAll() {
        scheduledTasks.forEach(ScheduledTask::cancel);
        scheduledTasks.clear();
    }

    public static void runTaskTimer(Runnable runnable, long delay) {
        try {
            scheduledTasks.add(CatScheduler.runTaskTimer(runnable, 0, delay));
        } catch (Exception e) {
            CatSeedLogin.instance.getLogger().severe(e.getMessage());
        }
    }
}
