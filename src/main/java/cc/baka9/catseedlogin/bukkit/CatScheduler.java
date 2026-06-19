package cc.baka9.catseedlogin.bukkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import space.arim.morepaperlib.MorePaperLib;
import space.arim.morepaperlib.scheduling.ScheduledTask;

public class CatScheduler {
    private static MorePaperLib morePaperLib;
    private static boolean folia;
    private static Method teleportAsync = null;

    public static void init(MorePaperLib mpl) {
        morePaperLib = mpl;
        folia = mpl.scheduling().isUsingFolia();
        teleportAsync = initTeleportAsync();
    }

    private static Method initTeleportAsync() {
        if (folia) {
            try {
                return Player.class.getMethod("teleportAsync", Location.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static void teleport(Player player, Location location) {
        if (location == null || location.getWorld() == null) return;
        if (!folia) {
            player.teleport(location);
            return;
        }
        morePaperLib.scheduling().entitySpecificScheduler(player).run(() -> {
            try {
                if (teleportAsync != null) {
                    teleportAsync.invoke(player, location);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }, null);
    }

    public static void updateInventory(Player player) {
        morePaperLib.scheduling().entitySpecificScheduler(player).run(player::updateInventory, null);
    }

    public static ScheduledTask runTaskAsync(Runnable runnable) {
        return morePaperLib.scheduling().asyncScheduler().run(runnable);
    }

    public static ScheduledTask runTaskTimer(Runnable runnable, long delay, long period) {
        return morePaperLib.scheduling().globalRegionalScheduler().runAtFixedRate(runnable, delay == 0 ? 1 : delay, period);
    }

    public static ScheduledTask runTask(Runnable runnable) {
        return morePaperLib.scheduling().globalRegionalScheduler().run(runnable);
    }

    public static ScheduledTask runTaskLater(Runnable runnable, long delay) {
        return morePaperLib.scheduling().globalRegionalScheduler().runDelayed(runnable, delay);
    }

    public static ScheduledTask runTaskLaterAsync(Runnable runnable, long delay) {
        return morePaperLib.scheduling().asyncScheduler().runDelayed(runnable, java.time.Duration.ofMillis(delay * 50));
    }

    public static ScheduledTask runTaskTimerAsync(Runnable runnable, long delay, long period) {
        return morePaperLib.scheduling().asyncScheduler().runAtFixedRate(runnable, java.time.Duration.ofMillis(delay * 50), java.time.Duration.ofMillis(period * 50));
    }
}