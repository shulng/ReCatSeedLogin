package cc.baka9.catseedlogin.velocity;

import cc.baka9.catseedlogin.common.config.ConfigManager;
import cc.baka9.catseedlogin.common.i18n.I18n;
import cc.baka9.catseedlogin.velocity.command.VelocityCommands;
import cc.baka9.catseedlogin.velocity.communication.VelocityCommunication;
import cc.baka9.catseedlogin.velocity.config.VelocityPlatformAdapter;
import cc.baka9.catseedlogin.velocity.listener.VelocityListeners;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

/** Velocity 端插件主类。 */
@Plugin(
    id = "catseedlogin",
    name = "CatSeedLogin",
    version = "${version}",
    description = "CatSeedLogin的Velocity适配版本，提供跨服登录验证功能",
    authors = {"shulng"})
public class VelocityPlugin {

  private static VelocityPlugin instance;
  private final ProxyServer proxyServer;
  private final Logger logger;
  private final Path dataDirectory;
  private ConfigManager configManager;
  private VelocityPlatformAdapter platformAdapter;
  private VelocityCommunication communication;
  private VelocityListeners listeners;

  @Inject
  public VelocityPlugin(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
    this.proxyServer = proxyServer;
    this.logger = logger;
    this.dataDirectory = dataDirectory;
    instance = this;
  }

  public static VelocityPlugin getInstance() {
    return instance;
  }

  public Path getDataDirectory() {
    return dataDirectory;
  }

  public ProxyServer getProxyServer() {
    return proxyServer;
  }

  public Logger getLogger() {
    return logger;
  }

  @Subscribe
  public void onProxyInitialization(ProxyInitializeEvent event) {
    platformAdapter = new VelocityPlatformAdapter(this);
    configManager = new ConfigManager(platformAdapter);
    configManager.reload();
    communication = new VelocityCommunication(configManager, logger);
    listeners = new VelocityListeners(configManager, communication, proxyServer, logger);

    proxyServer.getEventManager().register(this, listeners);

    proxyServer
        .getCommandManager()
        .register(
            proxyServer
                .getCommandManager()
                .metaBuilder("CatSeedLoginVelocity")
                .aliases("cslv")
                .build(),
            new VelocityCommands(configManager, proxyServer, logger));

    logger.info("CatSeedLogin-Velocity has been enabled!");
  }

  public static ScheduledTask runAsync(Runnable runnable) {
    return instance.proxyServer.getScheduler().buildTask(instance, runnable).schedule();
  }

  public static ScheduledTask runAsyncDelayed(Runnable runnable, long delay, TimeUnit unit) {
    return instance
        .proxyServer
        .getScheduler()
        .buildTask(instance, runnable)
        .delay(delay, unit)
        .schedule();
  }

  public ConfigManager getConfigManager() {
    return configManager;
  }

  public VelocityPlatformAdapter getPlatformAdapter() {
    return platformAdapter;
  }

  public I18n getI18n() {
    return configManager.getI18n();
  }

  public VelocityCommunication getCommunication() {
    return communication;
  }

  public VelocityListeners getListeners() {
    return listeners;
  }
}
