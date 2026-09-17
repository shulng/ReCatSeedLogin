package cc.baka9.catseedlogin.bungee;

import cc.baka9.catseedlogin.bungee.command.CommandCatSeedLogin;
import cc.baka9.catseedlogin.bungee.config.BungeeConfigManager;
import cc.baka9.catseedlogin.bungee.config.BungeePlatformAdapter;
import cc.baka9.catseedlogin.bungee.listener.BungeeListeners;
import cc.baka9.catseedlogin.bungee.net.BungeeCommunication;
import cc.baka9.catseedlogin.common.i18n.I18n;
import cc.baka9.catseedlogin.common.platform.PluginContext;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class PluginMain extends Plugin implements PluginContext {
  public static PluginMain instance;
  private BungeeConfigManager configManager;
  private BungeePlatformAdapter platformAdapter;
  private BungeeCommunication communication;

  @Override
  public void onEnable() {
    instance = this;
    configManager = new BungeeConfigManager(this);
    platformAdapter = new BungeePlatformAdapter(this, configManager.getI18n());
    communication = new BungeeCommunication(configManager, getLogger());
    configManager.reload();
    getProxy()
        .getPluginManager()
        .registerListener(this, new BungeeListeners(configManager, communication));
    getProxy()
        .getPluginManager()
        .registerCommand(
            this,
            new CommandCatSeedLogin("CatSeedLoginBungee", "catseedlogin.admin", configManager, "cslb"));
  }

  public static ScheduledTask runAsync(Runnable runnable) {
    return instance.getProxy().getScheduler().runAsync(instance, runnable);
  }

  public BungeeConfigManager getConfigManager() {
    return configManager;
  }

  public BungeePlatformAdapter getPlatformAdapter() {
    return platformAdapter;
  }

  public I18n getI18n() {
    return configManager.getI18n();
  }

  public BungeeCommunication getCommunication() {
    return communication;
  }
}
