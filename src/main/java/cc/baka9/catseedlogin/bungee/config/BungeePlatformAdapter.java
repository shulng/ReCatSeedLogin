package cc.baka9.catseedlogin.bungee.config;

import cc.baka9.catseedlogin.bungee.PluginMain;
import cc.baka9.catseedlogin.common.api.PlatformAdapter;
import cc.baka9.catseedlogin.common.i18n.I18n;
import net.md_5.bungee.api.ProxyServer;

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
  public I18n getI18n() {
    return i18n;
  }
}
