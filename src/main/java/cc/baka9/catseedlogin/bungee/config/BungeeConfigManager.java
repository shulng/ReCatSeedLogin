package cc.baka9.catseedlogin.bungee.config;

import cc.baka9.catseedlogin.common.config.BaseConfigManager;
import cc.baka9.catseedlogin.common.config.JulConfigLogger;
import cc.baka9.catseedlogin.common.config.PluginContext;
import java.io.InputStream;

public class BungeeConfigManager extends BaseConfigManager {

  private final PluginContext plugin;

  public BungeeConfigManager(PluginContext plugin) {
    super(new JulConfigLogger(plugin.getLogger()));
    this.plugin = plugin;
    initConfig(plugin.getDataFolder(), "config.yml");
  }

  @Override
  public InputStream getResource(String name) {
    return plugin.getResourceAsStream(name);
  }
}
