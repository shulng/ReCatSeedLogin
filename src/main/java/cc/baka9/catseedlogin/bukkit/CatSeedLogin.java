package cc.baka9.catseedlogin.bukkit;

import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager;
import cc.baka9.catseedlogin.bukkit.config.BukkitPlatformAdapter;
import cc.baka9.catseedlogin.bukkit.database.*;
import cc.baka9.catseedlogin.bukkit.module.BukkitModuleManager;
import cc.baka9.catseedlogin.bukkit.module.CommandModule;
import cc.baka9.catseedlogin.bukkit.module.ListenerModule;
import cc.baka9.catseedlogin.bukkit.module.RuntimeModule;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.bukkit.object.TwoFactorState;
import cc.baka9.catseedlogin.bukkit.util.TwoFactorMessageUtil;
import cc.baka9.catseedlogin.common.i18n.I18n;
import cc.baka9.catseedlogin.common.util.SecretCodec;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
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

  /** 旧版独立失明插件，功能已合并到本插件，同时安装会重复施加失明效果。 */
  private static final String LEGACY_BLINDING_PLUGIN = "CatSeedLoginBlinding";

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

    initTwoFactorSupport();

    sql = configManager.isMySQL() ? new MySQL(this) : new SQLite(this);
    try {
      sql.init();
      Cache.refreshAll();
    } catch (Exception e) {
      getLogger().warning("§c加载数据库时出错");
      e.printStackTrace();
      getServer().getPluginManager().disablePlugin(this);
      return;
    }

    checkBlindingPluginConflict();

    if (configManager.isEmptyBackpack()) {
      try {
        Class.forName("com.comphenix.protocol.ProtocolLib");
        ProtocolLibListeners.enable();
        loadProtocolLib = true;
      } catch (ClassNotFoundException e) {
        getLogger().warning("服务器没有装载ProtocolLib插件，这将无法使用登录前隐藏背包");
      }
    }

    PluginContext.init(this, sql, loadProtocolLib);


    if (Bukkit.getPluginManager().getPlugin("floodgate") != null
        && configManager.isBedrockLoginBypass()) {
      getLogger().info("检测到floodgate，基岩版兼容已装载");
    }

    moduleManager = new BukkitModuleManager(this);
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

  /** 两步验证的地基：探测 adventure 可用性，并准备好 TOTP 密钥文件。 */
  private void initTwoFactorSupport() {
    TwoFactorMessageUtil.probe();
    try {
      SecretCodec.init(getDataFolder());
    } catch (Exception e) {
      getLogger().warning("初始化两步验证密钥文件失败，两步验证将不可用: " + e.getMessage());
    }
  }

  /** 登录前失明已内置，若服务器仍安装 CatSeedLoginBlinding 会重复施加失明效果，这里提示管理员移除。 */
  private void checkBlindingPluginConflict() {
    if (getServer().getPluginManager().getPlugin(LEGACY_BLINDING_PLUGIN) == null) return;
    getLogger().warning("检测到 " + LEGACY_BLINDING_PLUGIN + " 插件，与内置的登录前失明功能冲突！");
    getLogger()
        .warning("请删除 " + LEGACY_BLINDING_PLUGIN + " 插件，改用 settings.before-login.blinding 控制该功能");
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

    try {
      sql.closeConnection();
    } catch (Exception e) {
      getLogger().warning("关闭数据库连接时出错");
      e.printStackTrace();
    }
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
