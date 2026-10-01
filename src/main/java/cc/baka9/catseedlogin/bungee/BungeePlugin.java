package cc.baka9.catseedlogin.bungee;

import cc.baka9.catseedlogin.bungee.command.BungeeCommands;
import cc.baka9.catseedlogin.bungee.communication.BungeeCommunication;
import cc.baka9.catseedlogin.bungee.config.BungeePlatformAdapter;
import cc.baka9.catseedlogin.bungee.listener.BungeeListeners;
import cc.baka9.catseedlogin.common.config.ConfigManager;
import cc.baka9.catseedlogin.common.i18n.I18n;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

/** BungeeCord 端插件主类。 */
public class BungeePlugin extends Plugin {

  public static BungeePlugin instance;

  private ConfigManager configManager;
  private BungeePlatformAdapter platformAdapter;
  private BungeeCommunication communication;

  @Override
  public void onEnable() {
    instance = this;
    platformAdapter = new BungeePlatformAdapter(this);
    configManager = new ConfigManager(platformAdapter);
    configManager.reload();
    communication = new BungeeCommunication(configManager, getLogger());

    getProxy()
        .getPluginManager()
        .registerListener(this, new BungeeListeners(configManager, communication));
    getProxy()
        .getPluginManager()
        .registerCommand(
            this,
            new BungeeCommands(
                "CatSeedLoginBungee", "catseedlogin.admin", configManager, "cslb"));
  }

  public static ScheduledTask runAsync(Runnable runnable) {
    return instance.getProxy().getScheduler().runAsync(instance, runnable);
  }

  public ConfigManager getConfigManager() {
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
