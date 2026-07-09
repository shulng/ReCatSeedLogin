package cc.baka9.catseedlogin.bukkit;

import cc.baka9.catseedlogin.api.CatSeedLoginAPI;
import cc.baka9.catseedlogin.common.Version;
import cc.baka9.catseedlogin.common.config.ConfigLoader;
import cc.baka9.catseedlogin.common.i18n.I18n;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;

/**
 * Main Bukkit plugin entry point.
 * Thin launcher that wires dependencies together.
 */
public class CatSeedLogin extends JavaPlugin {

    private static CatSeedLogin instance;

    private ConfigLoader configLoader;
    private BukkitPlatformAdapter platformAdapter;
    private Cache cache;
    private Communication communication;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize config
        configLoader = new ConfigLoader(getDataFolder(), this::getResource, getLogger());
        configLoader.load();

        // Initialize i18n
        String language = configLoader.getString("language", "zh_CN");
        I18n i18n = new I18n(getDataFolder(), this::getResource, getLogger());
        i18n.setLocale(language);

        // Initialize platform adapter
        platformAdapter = new BukkitPlatformAdapter(this, i18n);

        // Initialize cache
        cache = new Cache(this);

        // Initialize API
        CatSeedLoginAPI.init(new BukkitCatSeedLoginPlugin(this));

        // Register commands
        registerCommands();

        // Start tasks
        startTasks();

        getLogger().info("CatSeedLogin v" + Version.VERSION + " enabled");
    }

    @Override
    public void onDisable() {
        // Cancel tasks
        // Close database
        // Stop communication
        if (communication != null) {
            communication.stop();
        }

        getLogger().info("CatSeedLogin disabled");
    }

    private void registerCommands() {
        // Register commands here
    }

    private void startTasks() {
        // Start recurring tasks
    }

    public static CatSeedLogin getInstance() {
        return instance;
    }

    public ConfigLoader getConfigLoader() {
        return configLoader;
    }

    public BukkitPlatformAdapter getPlatformAdapter() {
        return platformAdapter;
    }

    public Cache getCache() {
        return cache;
    }
}
