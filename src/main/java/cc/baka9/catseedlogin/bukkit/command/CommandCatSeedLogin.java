package cc.baka9.catseedlogin.bukkit.command;

import cc.baka9.catseedlogin.bukkit.BukkitContext;
import cc.baka9.catseedlogin.bukkit.communication.CommunicationServer;
import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.database.MySQL;
import cc.baka9.catseedlogin.bukkit.database.SQLite;
import cc.baka9.catseedlogin.bukkit.scheduler.CatScheduler;
import cc.baka9.catseedlogin.bukkit.session.LoginPlayerHelper;
import cc.baka9.catseedlogin.common.config.ConfigConstants;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import cc.baka9.catseedlogin.common.model.LoginPlayer;
import cc.baka9.catseedlogin.common.util.PasswordHelper;
import cc.baka9.catseedlogin.common.util.TabCompleteUtil;
import cc.baka9.catseedlogin.common.util.ValidationUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/** {@code /catseedlogin} 管理员指令：配置开关、数值设置、白名单管理与账号维护。 */
public class CommandCatSeedLogin implements CommandExecutor, TabCompleter {

  public static final String PERMISSION = "catseedlogin.command.catseedlogin";

  /** 所有管理子命令，执行分发与 TAB 补全共用同一份数据源。 */
  public static final List<String> SUB_COMMANDS =
      Collections.unmodifiableList(
          Arrays.asList(
              "reload",
              "setPwd",
              "delPlayer",
              "setIpCountLimit",
              "setIpRegCountLimit",
              "setIdLength",
              "setReenterInterval",
              "setAutoKick",
              "setSpawnLocation",
              "limitChineseID",
              "bedrockLoginBypass",
              "LoginwiththesameIP",
              "beforeLoginNoDamage",
              "afterLoginBack",
              "canTpSpawnLocation",
              "deathStateQuitRecordLocation",
              "commandWhiteListInfo",
              "commandWhiteListAdd",
              "commandWhiteListDel",
              "loopbackLoginBypass",
              "beforeLoginAllowChat",
              "blindingBeforeLogin"));

  private static BukkitConfigManager config() {
    return BukkitContext.getConfigManager();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 0) {
      return false;
    }
    return reload(sender, args)
        || setPwd(sender, args)
        || delPlayer(sender, args)
        || toggleLoopbackLoginBypass(sender, args)
        || toggleBeforeLoginAllowChat(sender, args)
        || toggleBlindingBeforeLogin(sender, args)
        || setIpCountLimit(sender, args)
        || toggleLimitChineseID(sender, args)
        || toggleBedrockLoginBypass(sender, args)
        || toggleLoginWithSameIP(sender, args)
        || setIdLength(sender, args)
        || toggleBeforeLoginNoDamage(sender, args)
        || setReenterInterval(sender, args)
        || toggleAfterLoginBack(sender, args)
        || setSpawnLocation(sender, args)
        || commandWhiteListInfo(sender, args)
        || commandWhiteListAdd(sender, args)
        || commandWhiteListDel(sender, args)
        || toggleCanTpSpawnLocation(sender, args)
        || setAutoKick(sender, args)
        || setIpRegCountLimit(sender, args)
        || toggleDeathStateQuitRecordLocation(sender, args);
  }

  // ---- Tab Complete ----

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String alias, String[] args) {
    if (!sender.hasPermission(PERMISSION)) {
      return Collections.emptyList();
    }
    if (args.length == 1) {
      return TabCompleteUtil.filter(SUB_COMMANDS, args[0]);
    }
    if (args.length == 2) {
      String sub = args[0].toLowerCase();
      if ("delplayer".equals(sub) || "setpwd".equals(sub)) {
        return TabCompleteUtil.filter(registeredAndOnlinePlayerNames(), args[1]);
      }
      if ("commandwhitelistdel".equals(sub)) {
        return TabCompleteUtil.filter(commandWhiteListRegexes(), args[1]);
      }
    }
    return Collections.emptyList();
  }

  /** 在线玩家名优先，其后补充数据库中已注册的玩家名。 */
  private static List<String> registeredAndOnlinePlayerNames() {
    Set<String> names = new LinkedHashSet<>();
    Bukkit.getOnlinePlayers().forEach(p -> names.add(p.getName()));
    if (Cache.isLoaded) {
      Cache.getAllLoginPlayer()
          .forEach(
              lp -> {
                if (lp != null && lp.getName() != null) {
                  names.add(lp.getName());
                }
              });
    }
    return new ArrayList<>(names);
  }

  private static List<String> commandWhiteListRegexes() {
    return config().getCommandWhiteList().stream()
        .map(Pattern::toString)
        .collect(Collectors.toList());
  }

  // ---- Helper: Boolean Toggle ----

  private static boolean toggle(
      CommandSender sender, String[] args, String subCommand, String path, boolean defaultValue,
      String label) {
    if (!args[0].equalsIgnoreCase(subCommand)) return false;
    try {
      boolean enabled = config().toggle(path, defaultValue);
      sender.sendMessage(
          enabled
              ? MessageKey.ADMIN_TOGGLE_ON.get(label)
              : MessageKey.ADMIN_TOGGLE_OFF.get(label));
    } catch (Exception e) {
      sender.sendMessage(MessageKey.ADMIN_SET_FAILED.get(e.getMessage()));
    }
    return true;
  }

  // ---- Toggle Settings ----

  private boolean toggleDeathStateQuitRecordLocation(CommandSender sender, String[] args) {
    return toggle(
        sender,
        args,
        "deathStateQuitRecordLocation",
        ConfigConstants.Path.SETTINGS_DEATH_STATE_QUIT_RECORD,
        true,
        "死亡状态退出游戏记录退出位置");
  }

  private boolean toggleCanTpSpawnLocation(CommandSender sender, String[] args) {
    return toggle(
        sender,
        args,
        "canTpSpawnLocation",
        ConfigConstants.Path.SETTINGS_CAN_TP_SPAWN_LOCATION,
        true,
        "登录之前强制在登陆地点");
  }

  private boolean toggleAfterLoginBack(CommandSender sender, String[] args) {
    return toggle(
        sender,
        args,
        "afterLoginBack",
        ConfigConstants.Path.SETTINGS_AFTER_LOGIN_BACK,
        true,
        "登陆之后返回下线地点");
  }

  private boolean toggleBeforeLoginNoDamage(CommandSender sender, String[] args) {
    return toggle(
        sender,
        args,
        "beforeLoginNoDamage",
        ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_NO_DAMAGE,
        true,
        "登陆之前不受到伤害");
  }

  private boolean toggleLimitChineseID(CommandSender sender, String[] args) {
    return toggle(
        sender,
        args,
        "limitChineseID",
        ConfigConstants.Path.SETTINGS_LIMIT_CHINESE_ID,
        true,
        "限制中文游戏名");
  }

  private boolean toggleBedrockLoginBypass(CommandSender sender, String[] args) {
    return toggle(
        sender,
        args,
        "bedrockLoginBypass",
        ConfigConstants.Path.BEDROCK_LOGIN_BYPASS,
        true,
        "基岩版玩家登录跳过");
  }

  private boolean toggleLoginWithSameIP(CommandSender sender, String[] args) {
    return toggle(
        sender,
        args,
        "LoginwiththesameIP",
        ConfigConstants.Path.SAME_IP_ENABLED,
        false,
        "同IP玩家登录跳过");
  }

  private boolean toggleLoopbackLoginBypass(CommandSender sender, String[] args) {
    return toggle(
        sender,
        args,
        "loopbackLoginBypass",
        ConfigConstants.Path.SETTINGS_LOOPBACK_LOGIN_BYPASS,
        false,
        "本地回环地址登录跳过");
  }

  private boolean toggleBeforeLoginAllowChat(CommandSender sender, String[] args) {
    return toggle(
        sender,
        args,
        "beforeLoginAllowChat",
        ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_ALLOW_CHAT,
        false,
        "登陆之前允许发消息");
  }

  private boolean toggleBlindingBeforeLogin(CommandSender sender, String[] args) {
    return toggle(
        sender,
        args,
        "blindingBeforeLogin",
        ConfigConstants.Path.SETTINGS_BLINDING_BEFORE_LOGIN,
        false,
        "登陆之前失明效果");
  }

  // ---- Number Settings ----

  private boolean setAutoKick(CommandSender sender, String[] args) {
    if (args.length < 2 || !args[0].equalsIgnoreCase("setAutoKick")) return false;
    try {
      int seconds = Integer.parseInt(args[1]);
      config().set(ConfigConstants.Path.SETTINGS_AUTO_KICK, seconds);
      sender.sendMessage(
          seconds > 0
              ? MessageKey.ADMIN_AUTO_KICK_SET.get(seconds)
              : MessageKey.ADMIN_AUTO_KICK_DISABLED.get());
    } catch (NumberFormatException e) {
      sender.sendMessage(MessageKey.ADMIN_ENTER_NUMBER.get());
    }
    return true;
  }

  private boolean setReenterInterval(CommandSender sender, String[] args) {
    if (args.length < 2 || !args[0].equalsIgnoreCase("setReenterInterval")) return false;
    try {
      long interval = Long.parseLong(args[1]);
      config().set(ConfigConstants.Path.SETTINGS_REENTER_INTERVAL, interval);
      sender.sendMessage(MessageKey.ADMIN_REENTER_INTERVAL_SET.get(interval));
    } catch (NumberFormatException e) {
      sender.sendMessage(MessageKey.ADMIN_ENTER_NUMBER.get());
    }
    return true;
  }

  private boolean setIdLength(CommandSender sender, String[] args) {
    if (args.length < 3 || !args[0].equalsIgnoreCase("setIdLength")) return false;
    try {
      int min = Integer.parseInt(args[1]);
      int max = Integer.parseInt(args[2]);
      config().set(ConfigConstants.Path.SETTINGS_MIN_LENGTH_ID, min);
      config().set(ConfigConstants.Path.SETTINGS_MAX_LENGTH_ID, max);
      sender.sendMessage(MessageKey.ADMIN_ID_LENGTH_SET.get(min, max));
    } catch (NumberFormatException e) {
      sender.sendMessage(MessageKey.ADMIN_ENTER_NUMBER.get());
    }
    return true;
  }

  private boolean setIpCountLimit(CommandSender sender, String[] args) {
    if (args.length < 2 || !args[0].equalsIgnoreCase("setIpCountLimit")) return false;
    try {
      int limit = Integer.parseInt(args[1]);
      config().set(ConfigConstants.Path.SETTINGS_IP_COUNT_LIMIT, limit);
      sender.sendMessage(MessageKey.ADMIN_IP_LOGIN_LIMIT_SET.get(limit));
    } catch (NumberFormatException e) {
      sender.sendMessage(MessageKey.ADMIN_ENTER_NUMBER.get());
    }
    return true;
  }

  private boolean setIpRegCountLimit(CommandSender sender, String[] args) {
    if (args.length < 2 || !args[0].equalsIgnoreCase("setIpRegCountLimit")) return false;
    try {
      int limit = Integer.parseInt(args[1]);
      config().set(ConfigConstants.Path.SETTINGS_IP_REGISTER_LIMIT, limit);
      sender.sendMessage(MessageKey.ADMIN_IP_REG_LIMIT_SET.get(limit));
    } catch (NumberFormatException e) {
      sender.sendMessage(MessageKey.ADMIN_ENTER_NUMBER.get());
    }
    return true;
  }

  // ---- Command Whitelist ----

  private boolean commandWhiteListInfo(CommandSender sender, String[] args) {
    if (!args[0].equalsIgnoreCase("commandWhiteListInfo")) return false;
    sender.sendMessage(MessageKey.ADMIN_COMMAND_WHITELIST_INFO.get());
    config().getCommandWhiteList().forEach(regex -> sender.sendMessage(regex.toString()));
    return true;
  }

  private boolean commandWhiteListAdd(CommandSender sender, String[] args) {
    if (args.length < 2 || !args[0].equalsIgnoreCase("commandWhiteListAdd")) return false;
    String regex = joinArgs(args, 1);
    try {
      Pattern.compile(regex);
    } catch (Exception e) {
      sender.sendMessage(MessageKey.ADMIN_SET_FAILED.get(e.getMessage()));
      return true;
    }
    List<String> patterns = commandWhiteListRegexes();
    if (patterns.contains(regex)) {
      sender.sendMessage(MessageKey.ADMIN_COMMAND_WHITELIST_ALREADY_EXISTS.get(regex));
      return true;
    }
    patterns.add(regex);
    config().setCommandWhiteList(patterns);
    sender.sendMessage(MessageKey.ADMIN_COMMAND_WHITELIST_ADDED.get(regex));
    return true;
  }

  private boolean commandWhiteListDel(CommandSender sender, String[] args) {
    if (args.length < 2 || !args[0].equalsIgnoreCase("commandWhiteListDel")) return false;
    String regex = joinArgs(args, 1);
    List<String> patterns = commandWhiteListRegexes();
    if (!patterns.remove(regex)) {
      sender.sendMessage(MessageKey.ADMIN_COMMAND_WHITELIST_NOT_EXISTS.get(regex));
      return true;
    }
    config().setCommandWhiteList(patterns);
    sender.sendMessage(MessageKey.ADMIN_COMMAND_WHITELIST_REMOVED.get(regex));
    return true;
  }

  private static String joinArgs(String[] args, int from) {
    String[] cmd = new String[args.length - from];
    System.arraycopy(args, from, cmd, 0, cmd.length);
    return String.join(" ", cmd);
  }

  // ---- Spawn Location ----

  private boolean setSpawnLocation(CommandSender sender, String[] args) {
    if (!args[0].equalsIgnoreCase("setSpawnLocation")) return false;
    if (!(sender instanceof Player)) {
      sender.sendMessage(MessageKey.CANNOT_USE_FROM_CONSOLE.get());
      return true;
    }
    config().setSpawnLocation(((Player) sender).getLocation());
    sender.sendMessage(MessageKey.SPAWN_LOCATION_SET_MSG.get());
    return true;
  }

  // ---- Reload ----

  private boolean reload(CommandSender sender, String[] args) {
    if (!args[0].equalsIgnoreCase("reload")) return false;
    config().reload();
    try {
      BukkitContext.getSql().closeConnection();
    } catch (Exception e) {
      BukkitContext.getLogger().warning("§c关闭旧数据库连接时出错");
      e.printStackTrace();
    }
    BukkitContext.setSql(
        config().isMySQL()
            ? new MySQL(BukkitContext.getPlugin(), config())
            : new SQLite(BukkitContext.getPlugin()));
    try {
      BukkitContext.getSql().init();
      Cache.refreshAllSync();
    } catch (Exception e) {
      BukkitContext.getLogger().warning("§c加载数据库时出错");
      e.printStackTrace();
    }
    try {
      CommunicationServer.stopAsync();
    } catch (Exception e) {
      BukkitContext.getLogger().warning("§c停止通信服务时出错");
      e.printStackTrace();
    }
    if (config().isProxyEnabled()) {
      try {
        CommunicationServer.startAsync();
      } catch (Exception e) {
        BukkitContext.getLogger().warning("§c启动通信服务时出错");
        e.printStackTrace();
      }
    }
    sender.sendMessage(MessageKey.CONFIG_RELOADED_MSG.get());
    return true;
  }

  // ---- Delete Player ----

  private boolean delPlayer(CommandSender sender, String[] args) {
    if (args.length < 2 || !args[0].equalsIgnoreCase("delplayer")) return false;
    String name = args[1];
    LoginPlayer lp = Cache.getIgnoreCase(name);
    if (lp == null) {
      sender.sendMessage(MessageKey.ACCOUNT_NOT_EXISTS.get(name));
      return true;
    }
    delPlayerAsync(sender, lp);
    return true;
  }

  private void delPlayerAsync(CommandSender sender, LoginPlayer lp) {
    CatScheduler.runTaskAsync(
        () -> {
          try {
            BukkitContext.getSql().del(lp.getName());
            Cache.refresh(lp.getName());
            LoginPlayerHelper.remove(lp);
            sender.sendMessage(MessageKey.ACCOUNT_DELETED.get(lp.getName()));
            kickPlayerIfOnline(lp.getName());
          } catch (Exception e) {
            sender.sendMessage(MessageKey.DATABASE_ERROR.get());
            e.printStackTrace();
          }
        });
  }

  private static void kickPlayerIfOnline(String name) {
    CatScheduler.runTask(
        () -> {
          Player p = Bukkit.getPlayerExact(name);
          if (p != null && p.isOnline()) {
            p.kickPlayer(MessageKey.ACCOUNT_DELETED_KICK.get());
          }
        });
  }

  // ---- Set Password ----

  private boolean setPwd(CommandSender sender, String[] args) {
    if (args.length < 3 || !args[0].equalsIgnoreCase("setpwd")) return false;
    String name = args[1];
    String pwd = args[2];
    if (ValidationUtil.isPasswordTooSimple(pwd)) {
      sender.sendMessage(MessageKey.PASSWORD_TOO_SIMPLE_MSG.get());
      return true;
    }
    sender.sendMessage(MessageKey.SETTING_PASSWORD.get());
    CatScheduler.runTaskAsync(() -> setPwdLookup(sender, name, pwd));
    return true;
  }

  private void setPwdLookup(CommandSender sender, String name, String pwd) {
    LoginPlayer lp = Cache.getIgnoreCase(name);
    if (lp == null) {
      setPwdRegisterNew(sender, name, pwd);
    } else {
      setPwdUpdateExisting(sender, lp, pwd);
    }
  }

  private void setPwdRegisterNew(CommandSender sender, String name, String pwd) {
    try {
      LoginPlayer lp = PasswordHelper.registerNewPlayer(name, pwd);
      BukkitContext.getSql().add(lp);
      Cache.refresh(lp.getName());
      sender.sendMessage(MessageKey.ACCOUNT_NOT_EXISTS_REGISTERED.get());
    } catch (Exception e) {
      sender.sendMessage(MessageKey.DATABASE_ERROR.get());
      e.printStackTrace();
    }
  }

  private void setPwdUpdateExisting(CommandSender sender, LoginPlayer lp, String pwd) {
    try {
      LoginPlayer copy = PasswordHelper.updatePassword(lp, pwd);
      BukkitContext.getSql().edit(copy);
      Cache.refresh(copy.getName());
      LoginPlayerHelper.remove(lp);
      sender.sendMessage(MessageKey.PASSWORD_SET_MSG.get(lp.getName()));
      notifyPlayerPasswordChanged(lp);
    } catch (Exception e) {
      sender.sendMessage(MessageKey.DATABASE_ERROR.get());
      e.printStackTrace();
    }
  }

  private void notifyPlayerPasswordChanged(LoginPlayer lp) {
    CatScheduler.runTask(
        () -> {
          Player p = Bukkit.getPlayer(lp.getName());
          if (p == null || !p.isOnline()) return;
          p.sendMessage(MessageKey.PASSWORD_RESET_BY_ADMIN.get());
          if (!config().isCanTpSpawnLocation()) return;
          CatScheduler.teleport(p, config().getBukkitSpawnLocation());
          if (BukkitContext.isLoadProtocolLib()) {
            LoginPlayerHelper.sendBlankInventoryPacket(p);
          }
        });
  }
}
