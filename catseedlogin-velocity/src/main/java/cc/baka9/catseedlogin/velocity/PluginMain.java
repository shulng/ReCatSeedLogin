package cc.baka9.catseedlogin.velocity;

import cc.baka9.catseedlogin.api.CatSeedLoginAPI;
import cc.baka9.catseedlogin.common.Version;
import cc.baka9.catseedlogin.common.config.ConfigLoader;
import cc.baka9.catseedlogin.common.i18n.I18n;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * Velocity plugin main class.
 */
@Plugin(id = "catseedlogin", name = "CatSeedLogin", version = "${version}")
public class PluginMain {

    private static PluginMain instance;

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Path dataDirectory;

    private ConfigLoader configLoader;
    private VelocityPlatformAdapter platformAdapter;
    private VelocityCommunication communication;
    private VelocitySessionManager sessionManager;

    public PluginMain(ProxyServer proxyServer, Logger logger, @com.velocitypowered.api.plugin.annotation.DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;

        // Initialize config
        ConfigLoader.ResourceProvider resourceProvider = new ConfigLoader.ResourceProvider() {
            @Override
            public java.io.InputStream getResourceAsStream(String name) {
                return getClass().getClassLoader().getResourceAsStream(name);
            }
        };
        configLoader = new ConfigLoader(dataDirectory.toFile(), resourceProvider, null);
        configLoader.load();

        // Initialize i18n
        String language = configLoader.getString("language", "zh_CN");
        I18n.ResourceProvider i18nProvider = new I18n.ResourceProvider() {
            @Override
            public java.io.InputStream getResourceAsStream(String name) {
                return getClass().getClassLoader().getResourceAsStream(name);
            }
        };
        I18n i18n = new I18n(dataDirectory.toFile(), i18nProvider, null);
        i18n.setLocale(language);

        // Initialize platform adapter
        platformAdapter = new VelocityPlatformAdapter(this, i18n);

        // Initialize session manager
        sessionManager = new VelocitySessionManager();

        // Initialize communication
        communication = new VelocityCommunication(this);

        // Initialize API
        CatSeedLoginAPI.init(new VelocityCatSeedLoginPlugin(this));

        // Register listeners
        proxyServer.getEventManager().register(this, new Listeners(this));

        // Register commands
        proxyServer.getCommandManager().register(
                proxyServer.getCommandManager().metaBuilder("cslv").aliases("cslogin").build(),
                new Commands(this)
        );

        logger.info("CatSeedLogin Velocity v" + Version.VERSION + " enabled");
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

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public ConfigLoader getConfigLoader() {
        return configLoader;
    }

    public VelocityPlatformAdapter getPlatformAdapter() {
        return platformAdapter;
    }

    public VelocitySessionManager getSessionManager() {
        return sessionManager;
    }

    public VelocityCommunication getCommunication() {
        return communication;
    }

    public void runAsync(Runnable task) {
        proxyServer.getScheduler().buildTask(this, task).schedule();
    }

    public void runAsyncDelayed(Runnable task, long delay, java.util.concurrent.TimeUnit unit) {
        proxyServer.getScheduler().buildTask(this, task).delay(delay, unit).schedule();
    }
}
