package cc.baka9.catseedlogin.bungee.config;

import cc.baka9.catseedlogin.common.config.BaseConfigManager;
import cc.baka9.catseedlogin.common.config.PluginContext;
import cc.baka9.catseedlogin.common.config.YamlConfiguration;

import java.io.File;
import java.io.InputStream;

public class BungeeConfigManager extends BaseConfigManager {

    private final PluginContext plugin;

    public BungeeConfigManager(PluginContext plugin) {
        super();
        this.plugin = plugin;
        initConfig(plugin.getDataFolder(), "config.yml");
    }

    @Override
    public InputStream getResource(String name) {
        return plugin.getResourceAsStream(name);
    }

    public File getDataFolder() {
        return dataFolder;
    }
}
