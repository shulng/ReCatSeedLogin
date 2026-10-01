package cc.baka9.catseedlogin.bukkit;

import cc.baka9.catseedlogin.bukkit.command.*;
import cc.baka9.catseedlogin.bukkit.communication.CommunicationServer;
import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager;
import cc.baka9.catseedlogin.bukkit.config.BukkitPlatformAdapter;
import cc.baka9.catseedlogin.bukkit.database.*;
import cc.baka9.catseedlogin.bukkit.listener.BlindingListeners;
import cc.baka9.catseedlogin.bukkit.listener.BukkitListeners;
import cc.baka9.catseedlogin.bukkit.listener.ProtocolLibListeners;
import cc.baka9.catseedlogin.bukkit.scheduler.CatScheduler;
import cc.baka9.catseedlogin.bukkit.session.LoginPlayerHelper;
import cc.baka9.catseedlogin.bukkit.task.Task;
import cc.baka9.catseedlogin.common.i18n.I18n;
import cc.baka9.catseedlogin.common.util.TabCompleteUtil;
import cn.handyplus.lib.adapter.HandySchedulerUtil;
import java.util.Arrays;
import java.util.Collections;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import space.arim.morepaperlib.MorePaperLib;

/** Bukkit / Spigot / Paper / Folia 端插件主类。 */
public class CatSeedLogin extends JavaPlugin implements Listener {

  public static volatile CatSeedLogin instance;

  private static volatile MorePaperLib morePaperLib;

  private BukkitConfigManager configManager;
  private BukkitPlatformAdapter platformAdapter;
  private boolean protocolLibLoaded;

  @Override
  public void onEnable() {
    instance = this;
    morePaperLib = new MorePaperLib(this);
    CatScheduler.init(morePaperLib);
    HandySchedulerUtil.init(this);
    getServer().getPluginManager().registerEvents(this, this);

    platformAdapter = new BukkitPlatformAdapter(this);
    configManager = new BukkitConfigManager(platformAdapter);
    configManager.reload();

    SQL sql;
    try {
      sql = configManager.isMySQL() ? new MySQL(this, configManager) : new SQLite(this);
      sql.init();
    } catch (Exception e) {
      getLogger().warning("§c加载数据库时出错");
      e.printStackTrace();
      getServer().getPluginManager().disablePlugin(this);
      return;
    }

    BukkitContext.init(this, sql);
    Cache.refreshAll();

    getServer().getPluginManager().registerEvents(new BukkitListeners(), this);
    getServer().getPluginManager().registerEvents(new BlindingListeners(), this);

    protocolLibLoaded = false;
    if (configManager.isEmptyBackpack()) {
      try {
        Class.forName("com.comphenix.protocol.ProtocolLib");
        ProtocolLibListeners.enable();
        protocolLibLoaded = true;
      } catch (ClassNotFoundException e) {
        getLogger().warning("服务器没有装载ProtocolLib插件，这将无法使用登录前隐藏背包");
      }
    }

    if (configManager.isProxyEnabled()) {
      CommunicationServer.startAsync();
    }

    if (Bukkit.getPluginManager().getPlugin("floodgate") != null
        && configManager.isBedrockLoginBypass()) {
      getLogger().info("检测到floodgate，基岩版兼容已装载");
    }

    registerCommands();

    Task.runAll();
  }

  private void registerCommands() {
    registerLoginCommand();
    registerRegisterCommand();
    registerChangePasswordCommand();
    registerBindEmailCommand();
    registerResetPasswordCommand();
    registerCatSeedLoginCommand();
  }

  private void registerLoginCommand() {
    PluginCommand cmd = getServer().getPluginCommand("login");
    if (cmd == null) return;
    cmd.setExecutor(new CommandLogin());
    cmd.setTabCompleter(
        (commandSender, command, s, args) ->
            args.length == 1
                ? TabCompleteUtil.filter(Collections.singletonList("密码"), args[0])
                : Collections.emptyList());
  }

  private void registerRegisterCommand() {
    PluginCommand cmd = getServer().getPluginCommand("register");
    if (cmd == null) return;
    cmd.setExecutor(new CommandRegister());
    cmd.setTabCompleter(
        (commandSender, command, s, args) ->
            args.length == 1
                ? TabCompleteUtil.filter(Collections.singletonList("密码 重复密码"), args[0])
                : Collections.emptyList());
  }

  private void registerChangePasswordCommand() {
    PluginCommand cmd = getServer().getPluginCommand("changepassword");
    if (cmd == null) return;
    cmd.setExecutor(new CommandChangePassword());
    cmd.setTabCompleter(
        (commandSender, command, s, args) ->
            args.length == 1
                ? TabCompleteUtil.filter(Collections.singletonList("旧密码 新密码 重复新密码"), args[0])
                : Collections.emptyList());
  }

  private void registerBindEmailCommand() {
    PluginCommand bindemail = getServer().getPluginCommand("bindemail");
    if (bindemail == null) return;
    bindemail.setExecutor(new CommandBindEmail());
    bindemail.setTabCompleter(
        (commandSender, command, s, args) -> {
          if (args.length == 1) {
            return TabCompleteUtil.filter(Arrays.asList("set", "verify"), args[0]);
          }
          if (args.length == 2) {
            if ("set".equalsIgnoreCase(args[0])) {
              return TabCompleteUtil.filter(Collections.singletonList("需要绑定的邮箱"), args[1]);
            }
            if ("verify".equalsIgnoreCase(args[0])) {
              return TabCompleteUtil.filter(Collections.singletonList("邮箱获取的验证码"), args[1]);
            }
          }
          return Collections.emptyList();
        });
  }

  private void registerResetPasswordCommand() {
    PluginCommand resetpassword = getServer().getPluginCommand("resetpassword");
    if (resetpassword == null) return;
    resetpassword.setExecutor(new CommandResetPassword());
    resetpassword.setTabCompleter(
        (commandSender, command, s, args) -> {
          if (args.length == 1) {
            return TabCompleteUtil.filter(Arrays.asList("forget", "re"), args[0]);
          }
          if (args.length == 2 && "re".equalsIgnoreCase(args[0])) {
            return TabCompleteUtil.filter(Collections.singletonList("验证码"), args[1]);
          }
          if (args.length == 3 && "re".equalsIgnoreCase(args[0])) {
            return TabCompleteUtil.filter(Collections.singletonList("新密码"), args[2]);
          }
          return Collections.emptyList();
        });
  }

  private void registerCatSeedLoginCommand() {
    PluginCommand cmd = getServer().getPluginCommand("catseedlogin");
    if (cmd == null) return;
    CommandCatSeedLogin executor = new CommandCatSeedLogin();
    cmd.setExecutor(executor);
    cmd.setTabCompleter(executor);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    LoginPlayerHelper.onPlayerQuit(event.getPlayer().getName());
  }

  @Override
  public void onDisable() {
    Task.cancelAll();
    if (BukkitContext.isInitialized() && configManager != null) {
      Bukkit.getOnlinePlayers()
          .forEach(
              p -> {
                if (LoginPlayerHelper.isLogin(p.getName())
                    && (!p.isDead() || configManager.isDeathStateQuitRecordLocation())) {
                  OfflineLocationStore.saveSync(p);
                }
              });

      try {
        BukkitContext.getSql().closeConnection();
      } catch (Exception e) {
        getLogger().warning("关闭数据库连接时出错");
        e.printStackTrace();
      }
    }
    CommunicationServer.stop();
    super.onDisable();
  }

  public boolean isProtocolLibLoaded() {
    return protocolLibLoaded;
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
