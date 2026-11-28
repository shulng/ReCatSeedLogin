package cc.baka9.catseedlogin.bukkit;

import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager;
import cc.baka9.catseedlogin.bukkit.config.BukkitPlatformAdapter;
import cc.baka9.catseedlogin.bukkit.database.SQL;
import cc.baka9.catseedlogin.bukkit.module.BukkitModuleManager;
import cc.baka9.catseedlogin.bukkit.module.CommandModule;
import cc.baka9.catseedlogin.bukkit.module.DatabaseModule;
import cc.baka9.catseedlogin.bukkit.module.IntegrationModule;
import cc.baka9.catseedlogin.bukkit.module.ListenerModule;
import cc.baka9.catseedlogin.bukkit.module.RuntimeModule;
import cc.baka9.catseedlogin.bukkit.object.TwoFactorState;
import cc.baka9.catseedlogin.common.i18n.I18n;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import space.arim.morepaperlib.MorePaperLib;

public class CatSeedLogin extends JavaPlugin implements Listener {

  public static volatile CatSeedLogin instance;
  public static volatile SQL sql;
  public static volatile boolean loadProtocolLib = false;
  public static volatile MorePaperLib morePaperLib;

  private BukkitConfigManager configManager;
  private BukkitPlatformAdapter platformAdapter;
  private BukkitModuleManager moduleManager;

  @Override
  public void onEnable() {
    instance = this;
    morePaperLib = new MorePaperLib(this);
    CatScheduler.init(morePaperLib);
    getServer().getPluginManager().registerEvents(this, this);

    configManager = new BukkitConfigManager(this);
    platformAdapter = new BukkitPlatformAdapter(this, configManager.getI18n());

    try {
      configManager.reload();
      Config.load();
    } catch (Exception e) {
      e.printStackTrace();
      getServer().getLogger().warning("加载配置文件时出错，请检查你的配置文件。");
    }

    if (Bukkit.getPluginManager().getPlugin("floodgate") != null
        && configManager.isBedrockLoginBypass()) {
      getLogger().info("检测到floodgate，基岩版兼容已装载");
    }

    moduleManager = new BukkitModuleManager(this);
    moduleManager.register(new DatabaseModule());
    moduleManager.register(new IntegrationModule());
    moduleManager.register(new ListenerModule());
    moduleManager.register(new CommandModule());
    moduleManager.register(new RuntimeModule());
    try {
      moduleManager.enableAll();
    } catch (Exception e) {
      getLogger().severe("加载功能模块失败: " + e.getMessage());
      getServer().getPluginManager().disablePlugin(this);
      return;
    }

  }


  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    LoginPlayerHelper.onPlayerQuit(event.getPlayer().getName());
  }

  @Override
  public void onDisable() {
    if (moduleManager != null) moduleManager.disableAll();
    TwoFactorState.clear();
    Bukkit.getOnlinePlayers()
        .forEach(p -> {
          if (LoginPlayerHelper.isLogin(p.getName())
              && (!p.isDead() || configManager.isDeathStateQuitRecordLocation())) {
            Config.setOfflineLocationSync(p);
          }
        });

    super.onDisable();
  }

  public void runTaskAsync(Runnable runnable) {
    if (runnable != null) {
      CatScheduler.runTaskAsync(runnable);
    }
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
