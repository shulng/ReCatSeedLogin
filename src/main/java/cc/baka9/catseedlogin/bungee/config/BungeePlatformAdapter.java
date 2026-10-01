package cc.baka9.catseedlogin.bungee.config;

import cc.baka9.catseedlogin.bungee.BungeePlugin;
import cc.baka9.catseedlogin.common.api.PlatformAdapter;
import java.io.File;
import java.io.InputStream;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/** BungeeCord 平台能力适配。 */
public class BungeePlatformAdapter implements PlatformAdapter {

  private final BungeePlugin plugin;

  public BungeePlatformAdapter(BungeePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public String getName() {
    return "BungeeCord";
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
  public File getDataFolder() {
    return plugin.getDataFolder();
  }

  @Override
  public InputStream getResource(String name) {
    return plugin.getResourceAsStream(name);
  }

  @Override
  public void runAsync(Runnable task) {
    BungeePlugin.runAsync(task);
  }

  @Override
  public void runSync(Runnable task) {
    task.run();
  }

  @Override
  public void runAsyncLater(Runnable task, long delayTicks) {
    ProxyServer.getInstance()
        .getScheduler()
        .schedule(plugin, task, delayTicks * 50, java.util.concurrent.TimeUnit.MILLISECONDS);
  }

  @Override
  public void runSyncLater(Runnable task, long delayTicks) {
    runAsyncLater(task, delayTicks);
  }

  @Override
  public void runAsyncTimer(Runnable task, long delayTicks, long periodTicks) {
    ProxyServer.getInstance()
        .getScheduler()
        .schedule(
            plugin,
            task,
            delayTicks * 50,
            periodTicks * 50,
            java.util.concurrent.TimeUnit.MILLISECONDS);
  }

  @Override
  public void runSyncTimer(Runnable task, long delayTicks, long periodTicks) {
    runAsyncTimer(task, delayTicks, periodTicks);
  }

  @Override
  public Object getPlatformPlayer(String name) {
    return ProxyServer.getInstance().getPlayer(name);
  }

  @Override
  public boolean isPlayerOnline(String name) {
    return ProxyServer.getInstance().getPlayer(name) != null;
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
