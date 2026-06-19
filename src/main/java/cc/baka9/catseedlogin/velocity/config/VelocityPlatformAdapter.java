package cc.baka9.catseedlogin.velocity.config;

import cc.baka9.catseedlogin.velocity.PluginMain;
import cc.baka9.catseedlogin.common.Version;
import cc.baka9.catseedlogin.common.api.PlatformAdapter;
import cc.baka9.catseedlogin.common.i18n.I18n;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.concurrent.TimeUnit;

public class VelocityPlatformAdapter implements PlatformAdapter {

    private final PluginMain plugin;
    private final I18n i18n;

    public VelocityPlatformAdapter(PluginMain plugin, I18n i18n) {
        this.plugin = plugin;
        this.i18n = i18n;
    }

    @Override
    public String getName() {
        return "Velocity";
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
        plugin.getLogger().warn(message);
    }

    @Override
    public void logError(String message) {
        plugin.getLogger().error(message);
    }

    @Override
    public void logError(String message, Throwable throwable) {
        plugin.getLogger().error(message, throwable);
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
        plugin.getProxyServer().getScheduler()
                .buildTask(plugin, task)
                .delay(delayTicks * 50, TimeUnit.MILLISECONDS)
                .schedule();
    }

    @Override
    public void runSyncLater(Runnable task, long delayTicks) {
        runAsyncLater(task, delayTicks);
    }

    @Override
    public void runAsyncTimer(Runnable task, long delayTicks, long periodTicks) {
        plugin.getProxyServer().getScheduler()
                .buildTask(plugin, task)
                .delay(delayTicks * 50, TimeUnit.MILLISECONDS)
                .repeat(periodTicks * 50, TimeUnit.MILLISECONDS)
                .schedule();
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
        return plugin.getProxyServer().getPlayer(name).orElse(null);
    }

    @Override
    public boolean isPlayerOnline(String name) {
        return plugin.getProxyServer().getPlayer(name).isPresent();
    }

    @Override
    public void kickPlayer(String name, String reason) {
        plugin.getProxyServer().getPlayer(name).ifPresent(player -> 
            player.disconnect(net.kyori.adventure.text.Component.text(reason))
        );
    }

    @Override
    public void sendMessage(String playerName, String message) {
        plugin.getProxyServer().getPlayer(playerName).ifPresent(player -> 
            player.sendMessage(net.kyori.adventure.text.Component.text(message))
        );
    }

    @Override
    public void broadcast(String message) {
        ProxyServer proxyServer = plugin.getProxyServer();
        for (Player player : proxyServer.getAllPlayers()) {
            player.sendMessage(net.kyori.adventure.text.Component.text(message));
        }
    }
}
