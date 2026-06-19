package cc.baka9.catseedlogin.bungee.config;

import cc.baka9.catseedlogin.bungee.PluginMain;
import cc.baka9.catseedlogin.common.Version;
import cc.baka9.catseedlogin.common.api.PlatformAdapter;
import cc.baka9.catseedlogin.common.i18n.I18n;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

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
        return Version.VERSION;
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
        plugin.getLogger().severe(message);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void runAsync(Runnable task) {
        PluginMain.runAsync(task);
    }

    @Override
    public void runSync(Runnable task) {
        task.run();
    }

    @Override
    public void runAsyncLater(Runnable task, long delayTicks) {
        ProxyServer.getInstance().getScheduler().schedule(plugin, task, delayTicks * 50, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    @Override
    public void runSyncLater(Runnable task, long delayTicks) {
        runAsyncLater(task, delayTicks);
    }

    @Override
    public void runAsyncTimer(Runnable task, long delayTicks, long periodTicks) {
        ProxyServer.getInstance().getScheduler().schedule(plugin, task, delayTicks * 50, periodTicks * 50, java.util.concurrent.TimeUnit.MILLISECONDS);
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
        return player != null;
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
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            player.sendMessage(message);
        }
    }
}
