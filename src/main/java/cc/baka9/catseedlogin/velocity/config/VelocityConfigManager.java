package cc.baka9.catseedlogin.velocity.config;

import cc.baka9.catseedlogin.velocity.PluginMain;
import cc.baka9.catseedlogin.common.config.BaseConfigManager;

import java.io.File;
import java.io.InputStream;

public class VelocityConfigManager extends BaseConfigManager {

    private final PluginMain plugin;

    public VelocityConfigManager(PluginMain plugin) {
        super();
        this.plugin = plugin;
        initConfig(plugin.getDataDirectory().toFile(), "config.yml");
    }

    @Override
    public InputStream getResource(String name) {
        return getClass().getClassLoader().getResourceAsStream(name);
    }

    public File getDataFolder() {
        return dataFolder;
    }
}
