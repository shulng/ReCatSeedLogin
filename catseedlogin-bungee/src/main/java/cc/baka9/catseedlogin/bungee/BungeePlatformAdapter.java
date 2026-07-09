package cc.baka9.catseedlogin.bungee;

import cc.baka9.catseedlogin.common.api.PlatformAdapter;
import cc.baka9.catseedlogin.common.i18n.I18n;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.concurrent.TimeUnit;

/**
 * BungeeCord platform adapter.
 */
public class BungeePlatformAdapter implements PlatformAdapter {

    private final PluginMain plugin;
    private final I18n i18n;

    public BungeePlatformAdapter(PluginMain plugin, I18n i18n) {
        this.plugin = plugin;
        this.i18n = i18n;
    }

    @Override
    public String getName() {
        return "BungeeCord";
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
        ProxyServer.getInstance().getScheduler().schedule(plugin, task, 0, TimeUnit.MILLISECONDS);
    }

    @Override
    public void runSync(Runnable task) {
        task.run();
    }

    @Override
    public void runAsyncLater(Runnable task, long delayTicks) {
        long delayMs = delayTicks * 50;
        ProxyServer.getInstance().getScheduler().schedule(plugin, task, delayMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void runSyncLater(Runnable task, long delayTicks) {
        runAsyncLater(task, delayTicks);
    }

    @Override
    public void runAsyncTimer(Runnable task, long delayTicks, long periodTicks) {
        long delayMs = delayTicks * 50;
        long periodMs = periodTicks * 50;
        ProxyServer.getInstance().getScheduler().schedule(plugin, task, delayMs, periodMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void runSyncTimer(Runnable task, long delayTicks, long periodTicks) {
        runAsyncTimer(task, delayTicks, periodTicks);
    }

    @Override
    public I18n getI18n() {
        return i18n;
    }

    @Override
    public Object getPlatformPlayer(String name) {
        return ProxyServer.getInstance().getPlayer(name);
    }

    @Override
    public boolean isPlayerOnline(String name) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(name);
        return player != null && player.isConnected();
    }

    @Override
    public void kickPlayer(String name, String reason) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(name);
        if (player != null) {
            player.disconnect(reason);
        }
    }

    @Override
    public void sendMessage(String playerName, String message) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerName);
        if (player != null) {
            player.sendMessage(message);
        }
    }

    @Override
    public void broadcast(String message) {
        ProxyServer.getInstance().broadcast(message);
    }
}
