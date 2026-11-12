package cc.baka9.catseedlogin.bukkit.config;

import cc.baka9.catseedlogin.bukkit.CatScheduler;
import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.common.api.PlatformAdapter;
import cc.baka9.catseedlogin.common.i18n.I18n;

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
    CatScheduler.runTaskAsync(task);
  }

  @Override
  public void runSync(Runnable task) {
    CatScheduler.runTask(task);
  }

  @Override
  public void runAsyncLater(Runnable task, long delayTicks) {
    CatScheduler.runTaskLaterAsync(task, delayTicks);
  }

  @Override
  public void runSyncLater(Runnable task, long delayTicks) {
    CatScheduler.runTaskLater(task, delayTicks);
  }

  @Override
  public void runAsyncTimer(Runnable task, long delayTicks, long periodTicks) {
    CatScheduler.runTaskTimerAsync(task, delayTicks, periodTicks);
  }

  @Override
  public void runSyncTimer(Runnable task, long delayTicks, long periodTicks) {
    CatScheduler.runTaskTimer(task, delayTicks, periodTicks);
  }

  @Override
  public I18n getI18n() {
    return i18n;
  }
}
