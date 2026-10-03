package cc.baka9.catseedlogin.bukkit;

import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager;
import cc.baka9.catseedlogin.bukkit.config.BukkitPlatformAdapter;
import cc.baka9.catseedlogin.bukkit.lifecycle.BukkitShutdown;
import cc.baka9.catseedlogin.bukkit.lifecycle.BukkitStartup;
import cc.baka9.catseedlogin.bukkit.scheduler.CatScheduler;
import cc.baka9.catseedlogin.common.i18n.I18n;
import cn.handyplus.lib.adapter.HandySchedulerUtil;
import org.bukkit.plugin.java.JavaPlugin;
import space.arim.morepaperlib.MorePaperLib;

/** Bukkit / Spigot / Paper / Folia 端插件主类，只负责各模块的装载与卸载。 */
public class CatSeedLogin extends JavaPlugin {

  public static volatile CatSeedLogin instance;

  private BukkitConfigManager configManager;
  private BukkitPlatformAdapter platformAdapter;

  @Override
  public void onEnable() {
    instance = this;
    CatScheduler.init(new MorePaperLib(this));
    HandySchedulerUtil.init(this);

    platformAdapter = new BukkitPlatformAdapter(this);
    configManager = new BukkitConfigManager(platformAdapter);
    configManager.reload();
    configManager.ensureSpawnLocationPersisted();

    if (!BukkitStartup.enable(this, configManager)) {
      getServer().getPluginManager().disablePlugin(this);
    }
  }

  @Override
  public void onDisable() {
    BukkitShutdown.run(configManager);
    super.onDisable();
  }

  public BukkitConfigManager getConfigManager() {
    return configManager;
  }

  public BukkitPlatformAdapter getPlatformAdapter() {
    return platformAdapter;
  }

  public I18n getI18n() {
    return configManager.getI18n();
  }
}
