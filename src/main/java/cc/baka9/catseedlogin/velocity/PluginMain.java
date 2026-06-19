package cc.baka9.catseedlogin.velocity;

import cc.baka9.catseedlogin.velocity.config.VelocityConfigManager;
import cc.baka9.catseedlogin.velocity.config.VelocityPlatformAdapter;
import cc.baka9.catseedlogin.common.api.PlatformAdapter;
import cc.baka9.catseedlogin.common.i18n.I18n;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Plugin(
    id = "catseedlogin",
    name = "CatSeedLogin",
    version = "${version}",
    description = "CatSeedLogin的Velocity适配版本，提供跨服登录验证功能",
    authors = {"shulng"}
)
public class PluginMain {
    
    private static PluginMain instance;
    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Path dataDirectory;
    private VelocityConfigManager configManager;
    private VelocityPlatformAdapter platformAdapter;
    private VelocityCommunication communication;
    private Listeners listeners;
    
    @Inject
    public PluginMain(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        instance = this;
    }
    
    public Path getDataDirectory() {
        return dataDirectory;
    }
    
    public static PluginMain getInstance() {
        return instance;
    }
    
    public ProxyServer getProxyServer() {
        return proxyServer;
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        configManager = new VelocityConfigManager(this);
        platformAdapter = new VelocityPlatformAdapter(this, configManager.getI18n());
        communication = new VelocityCommunication(configManager, logger);
        listeners = new Listeners(configManager, communication, proxyServer, logger);
        configManager.reload();
        
        proxyServer.getEventManager().register(this, listeners);
        
        proxyServer.getCommandManager().register(
            proxyServer.getCommandManager().metaBuilder("CatSeedLoginVelocity")
                .aliases("cslv")
                .build(),
            new Commands(configManager, proxyServer, logger)
        );
        
        logger.info("CatSeedLogin-Velocity has been enabled!");
    }
    
    public static ScheduledTask runAsync(Runnable runnable) {
        return instance.proxyServer.getScheduler()
            .buildTask(instance, runnable)
            .schedule();
    }
    
    public static ScheduledTask runAsyncDelayed(Runnable runnable, long delay, TimeUnit unit) {
        return instance.proxyServer.getScheduler()
            .buildTask(instance, runnable)
            .delay(delay, unit)
            .schedule();
    }

    public VelocityConfigManager getConfigManager() {
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

    public Listeners getListeners() {
        return listeners;
    }
}