package cc.baka9.catseedlogin.bungee;

import cc.baka9.catseedlogin.bungee.config.BungeeConfigManager;
import cc.baka9.catseedlogin.common.api.Permissions;
import cc.baka9.catseedlogin.common.config.PluginContext;
import cc.baka9.catseedlogin.common.i18n.I18n;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class PluginMain extends Plugin implements PluginContext {
  public static PluginMain instance;
  private BungeeConfigManager configManager;
  private BungeeCommunication communication;
  private Listeners listeners;

  @Override
  public void onEnable() {
    instance = this;
    configManager = new BungeeConfigManager(this);
    communication = new BungeeCommunication(configManager, getLogger());
    configManager.reload();

    listeners = new Listeners(configManager, communication);
    getProxy().getPluginManager().registerListener(this, listeners);
    getProxy()
        .getPluginManager()
        .registerCommand(
            this,
            new BungeeCommands("CatSeedLoginBungee", Permissions.ADMIN, configManager, "cslb"));
  }

  public static ScheduledTask runAsync(Runnable runnable) {
    return instance.getProxy().getScheduler().runAsync(instance, runnable);
  }

  public static ScheduledTask runAsyncLater(Runnable runnable, long delayTicks) {
    return instance
        .getProxy()
        .getScheduler()
        .schedule(
            instance,
            runnable,
            delayTicks * 50,
            java.util.concurrent.TimeUnit.MILLISECONDS);
  }

  public BungeeConfigManager getConfigManager() {
    return configManager;
  }

  public I18n getI18n() {
    return configManager.getI18n();
  }

  public BungeeCommunication getCommunication() {
    return communication;
  }

  public Listeners getListeners() {
    return listeners;
  }
}
