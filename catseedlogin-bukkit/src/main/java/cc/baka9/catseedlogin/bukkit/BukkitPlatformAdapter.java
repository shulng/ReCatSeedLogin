package cc.baka9.catseedlogin.bukkit;

import cc.baka9.catseedlogin.common.api.PlatformAdapter;
import cc.baka9.catseedlogin.common.i18n.I18n;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Bukkit/Paper platform adapter.
 */
public class BukkitPlatformAdapter implements PlatformAdapter {

    private final CatSeedLogin plugin;
    private final I18n i18n;

    public BukkitPlatformAdapter(CatSeedLogin plugin, I18n i18n) {
        this.plugin = plugin;
        this.i18n = i18n;
    }

    @Override
    public String getName() {
        return "Bukkit";
    }

    @Override
    public String getVersion() {
        return cc.baka9.catseedlogin.common.Version.VERSION;
    }

    @Override
    public void logInfo(String message) {
        plugin.getLogger().info(message);
    }

    @Override
    public void logWarn(String message) {
        plugin.getLogger().warning(message);
    }

    @Override
    public void logError(String message) {
        plugin.getLogger().severe(message);
    }

    @Override
    public void logError(String message, Throwable throwable) {
        plugin.getLogger().log(java.util.logging.Level.SEVERE, message, throwable);
    }

    @Override
    public void runAsync(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    @Override
    public void runSync(Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    @Override
    public void runAsyncLater(Runnable task, long delayTicks) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
    }

    @Override
    public void runSyncLater(Runnable task, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    @Override
    public void runAsyncTimer(Runnable task, long delayTicks, long periodTicks) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
    }

    @Override
    public void runSyncTimer(Runnable task, long delayTicks, long periodTicks) {
        Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
    }

    @Override
    public I18n getI18n() {
        return i18n;
    }

    @Override
    public Object getPlatformPlayer(String name) {
        return Bukkit.getPlayerExact(name);
    }

    @Override
    public boolean isPlayerOnline(String name) {
        Player player = Bukkit.getPlayerExact(name);
        return player != null && player.isOnline();
    }

    @Override
    public void kickPlayer(String name, String reason) {
        Player player = Bukkit.getPlayerExact(name);
        if (player != null) {
            player.kickPlayer(reason);
        }
    }

    @Override
    public void sendMessage(String playerName, String message) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player != null) {
            player.sendMessage(message);
        }
    }

    @Override
    public void broadcast(String message) {
        Bukkit.broadcastMessage(message);
    }
}
