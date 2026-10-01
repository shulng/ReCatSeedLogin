package cc.baka9.catseedlogin.velocity.config;

import cc.baka9.catseedlogin.common.api.PlatformAdapter;
import cc.baka9.catseedlogin.velocity.VelocityPlugin;
import com.velocitypowered.api.proxy.Player;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;

/** Velocity 平台能力适配。 */
public class VelocityPlatformAdapter implements PlatformAdapter {

  private final VelocityPlugin plugin;

  public VelocityPlatformAdapter(VelocityPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public String getName() {
    return "Velocity";
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
  public File getDataFolder() {
    return plugin.getDataDirectory().toFile();
  }

  @Override
  public InputStream getResource(String name) {
    return getClass().getClassLoader().getResourceAsStream(name);
  }

  @Override
  public void runAsync(Runnable task) {
    VelocityPlugin.runAsync(task);
  }

  @Override
  public void runSync(Runnable task) {
    task.run();
  }

  @Override
  public void runAsyncLater(Runnable task, long delayTicks) {
    plugin
        .getProxyServer()
        .getScheduler()
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
    plugin
        .getProxyServer()
        .getScheduler()
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
  public Object getPlatformPlayer(String name) {
    return plugin.getProxyServer().getPlayer(name).orElse(null);
  }

  @Override
  public boolean isPlayerOnline(String name) {
    return plugin.getProxyServer().getPlayer(name).isPresent();
  }

  @Override
  public void kickPlayer(String name, String reason) {
    plugin
        .getProxyServer()
        .getPlayer(name)
        .ifPresent(player -> player.disconnect(Component.text(reason)));
  }

  @Override
  public void sendMessage(String playerName, String message) {
    plugin
        .getProxyServer()
        .getPlayer(playerName)
        .ifPresent(player -> player.sendMessage(Component.text(message)));
  }

  @Override
  public void broadcast(String message) {
    for (Player player : plugin.getProxyServer().getAllPlayers()) {
      player.sendMessage(Component.text(message));
    }
  }
}
