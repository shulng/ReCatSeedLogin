package cc.baka9.catseedlogin.velocity;

import cc.baka9.catseedlogin.common.api.PlatformAdapter;
import cc.baka9.catseedlogin.common.i18n.I18n;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Velocity platform adapter.
 */
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
        return cc.baka9.catseedlogin.common.Version.VERSION;
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
        plugin.getProxyServer().getScheduler().buildTask(plugin, task).schedule();
    }

    @Override
    public void runSync(Runnable task) {
        task.run();
    }

    @Override
    public void runAsyncLater(Runnable task, long delayTicks) {
        long delayMs = delayTicks * 50;
        plugin.getProxyServer().getScheduler().buildTask(plugin, task)
                .delay(delayMs, TimeUnit.MILLISECONDS).schedule();
    }

    @Override
    public void runSyncLater(Runnable task, long delayTicks) {
        runAsyncLater(task, delayTicks);
    }

    @Override
    public void runAsyncTimer(Runnable task, long delayTicks, long periodTicks) {
        long delayMs = delayTicks * 50;
        long periodMs = periodTicks * 50;
        plugin.getProxyServer().getScheduler().buildTask(plugin, task)
                .delay(delayMs, TimeUnit.MILLISECONDS)
                .repeat(periodMs, TimeUnit.MILLISECONDS).schedule();
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
        Optional<Player> player = plugin.getProxyServer().getPlayer(name);
        player.ifPresent(p -> p.disconnect(Component.text(reason)));
    }

    @Override
    public void sendMessage(String playerName, String message) {
        Optional<Player> player = plugin.getProxyServer().getPlayer(playerName);
        player.ifPresent(p -> p.sendMessage(Component.text(message)));
    }

    @Override
    public void broadcast(String message) {
        for (Player player : plugin.getProxyServer().getAllPlayers()) {
            player.sendMessage(Component.text(message));
        }
    }
}
