package cc.baka9.catseedlogin.bungee;

import cc.baka9.catseedlogin.api.CatSeedLoginAPI;
import cc.baka9.catseedlogin.common.Version;
import cc.baka9.catseedlogin.common.config.ConfigLoader;
import cc.baka9.catseedlogin.common.i18n.I18n;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * BungeeCord plugin main class.
 */
public class PluginMain extends Plugin {

    private static PluginMain instance;

    private ConfigLoader configLoader;
    private BungeePlatformAdapter platformAdapter;
    private BungeeCommunication communication;
    private BungeeSessionManager sessionManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize config
        ConfigLoader.ResourceProvider resourceProvider = new ConfigLoader.ResourceProvider() {
            @Override
            public InputStream getResourceAsStream(String name) {
                return getResourceAsStream(name);
            }
        };
        configLoader = new ConfigLoader(getDataFolder(), resourceProvider, getLogger());
        configLoader.load();

        // Initialize i18n
        String language = configLoader.getString("language", "zh_CN");
        I18n.ResourceProvider i18nProvider = new I18n.ResourceProvider() {
            @Override
            public InputStream getResourceAsStream(String name) {
                return getResourceAsStream(name);
            }
        };
        I18n i18n = new I18n(getDataFolder(), i18nProvider, getLogger());
        i18n.setLocale(language);

        // Initialize platform adapter
        platformAdapter = new BungeePlatformAdapter(this, i18n);

        // Initialize session manager
        sessionManager = new BungeeSessionManager();

        // Initialize communication
        communication = new BungeeCommunication(this);

        // Initialize API
        CatSeedLoginAPI.init(new BungeeCatSeedLoginPlugin(this));

        // Register listeners
        getProxy().getPluginManager().registerListener(this, new Listeners(this));

        // Register commands
        getProxy().getPluginManager().registerCommand(this, new BungeeCommands(this));

        // Start communication client
        if (configLoader.getBoolean("proxy.enabled", false)) {
            getProxy().getScheduler().schedule(this, new Runnable() {
                @Override
                public void run() {
                    try {
                        communication.start();
                    } catch (Exception e) {
                        getLogger().log(Level.SEVERE, "Failed to start communication", e);
                    }
                }
            }, 0, TimeUnit.SECONDS);
        }

        getLogger().info("CatSeedLogin BungeeCord v" + Version.VERSION + " enabled");
    }

    @Override
    public void onDisable() {
        if (communication != null) {
            communication.stop();
        }
        getLogger().info("CatSeedLogin BungeeCord disabled");
    }

    public static PluginMain getInstance() {
        return instance;
    }

    public ConfigLoader getConfigLoader() {
        return configLoader;
    }

    public BungeePlatformAdapter getPlatformAdapter() {
        return platformAdapter;
    }

    public BungeeSessionManager getSessionManager() {
        return sessionManager;
    }

    public static void runAsync(Runnable task) {
        ProxyServer.getInstance().getScheduler().runAsync(instance, task);
    }
}
