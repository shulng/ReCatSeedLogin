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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
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

  /** 子命令执行体，{@code args[0]} 为子命令名。返回 true 表示该指令已被处理。 */
  @FunctionalInterface
  private interface Handler {
    boolean execute(CommandSender sender, String[] args);
  }

  /** 子命令的参数补全来源。 */
  @FunctionalInterface
  private interface Completer {
    List<String> complete();
  }

  /**
   * 子命令注册表：名称、所需参数数量、执行逻辑与补全来源集中定义。
   *
   * <p>新增子命令只需在此添加一个枚举常量，指令分发与 TAB 补全自动生效。
   */
  private enum SubCommand {
    RELOAD("reload", 1, CommandCatSeedLogin::reload),
    SET_PWD("setPwd", 3, CommandCatSeedLogin::setPwd, CommandCatSeedLogin::playerNames),
    DEL_PLAYER("delPlayer", 2, CommandCatSeedLogin::delPlayer, CommandCatSeedLogin::playerNames),
    SET_SPAWN_LOCATION("setSpawnLocation", 1, CommandCatSeedLogin::setSpawnLocation),
    COMMAND_WHITELIST_INFO("commandWhiteListInfo", 1, CommandCatSeedLogin::commandWhiteListInfo),
    COMMAND_WHITELIST_ADD("commandWhiteListAdd", 2, CommandCatSeedLogin::commandWhiteListAdd),
    COMMAND_WHITELIST_DEL(
        "commandWhiteListDel",
        2,
        CommandCatSeedLogin::commandWhiteListDel,
        CommandCatSeedLogin::commandWhiteListRegexes),

    SET_IP_COUNT_LIMIT(
        "setIpCountLimit",
        2,
        (sender, args) ->
            setInt(
                sender,
                args,
                ConfigConstants.Path.SETTINGS_IP_COUNT_LIMIT,
                v -> MessageKey.ADMIN_IP_LOGIN_LIMIT_SET.get(v))),
    SET_IP_REG_COUNT_LIMIT(
        "setIpRegCountLimit",
        2,
        (sender, args) ->
            setInt(
                sender,
                args,
                ConfigConstants.Path.SETTINGS_IP_REGISTER_LIMIT,
                v -> MessageKey.ADMIN_IP_REG_LIMIT_SET.get(v))),
    SET_AUTO_KICK(
        "setAutoKick",
        2,
        (sender, args) ->
            setInt(
                sender,
                args,
                ConfigConstants.Path.SETTINGS_AUTO_KICK,
                v ->
                    v > 0
                        ? MessageKey.ADMIN_AUTO_KICK_SET.get(v)
                        : MessageKey.ADMIN_AUTO_KICK_DISABLED.get())),
    SET_REENTER_INTERVAL(
        "setReenterInterval",
        2,
        (sender, args) ->
            setLong(
                sender,
                args,
                ConfigConstants.Path.SETTINGS_REENTER_INTERVAL,
                v -> MessageKey.ADMIN_REENTER_INTERVAL_SET.get(v))),
    SET_ID_LENGTH("setIdLength", 3, CommandCatSeedLogin::setIdLength),

    LIMIT_CHINESE_ID(
        "limitChineseID", ConfigConstants.Path.SETTINGS_LIMIT_CHINESE_ID, true, "限制中文游戏名"),
    BEDROCK_LOGIN_BYPASS(
        "bedrockLoginBypass", ConfigConstants.Path.BEDROCK_LOGIN_BYPASS, true, "基岩版玩家登录跳过"),
    LOGIN_WITH_SAME_IP(
        "LoginwiththesameIP", ConfigConstants.Path.SAME_IP_ENABLED, false, "同IP玩家登录跳过"),
    LOOPBACK_LOGIN_BYPASS(
        "loopbackLoginBypass",
        ConfigConstants.Path.SETTINGS_LOOPBACK_LOGIN_BYPASS,
        false,
        "本地回环地址登录跳过"),
    BEFORE_LOGIN_NO_DAMAGE(
        "beforeLoginNoDamage",
        ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_NO_DAMAGE,
        true,
        "登陆之前不受到伤害"),
    AFTER_LOGIN_BACK(
        "afterLoginBack", ConfigConstants.Path.SETTINGS_AFTER_LOGIN_BACK, true, "登陆之后返回下线地点"),
    CAN_TP_SPAWN_LOCATION(
        "canTpSpawnLocation",
        ConfigConstants.Path.SETTINGS_CAN_TP_SPAWN_LOCATION,
        true,
        "登录之前强制在登陆地点"),
    DEATH_STATE_QUIT_RECORD_LOCATION(
        "deathStateQuitRecordLocation",
        ConfigConstants.Path.SETTINGS_DEATH_STATE_QUIT_RECORD,
        true,
        "死亡状态退出游戏记录退出位置"),
    BEFORE_LOGIN_ALLOW_CHAT(
        "beforeLoginAllowChat",
        ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_ALLOW_CHAT,
        false,
        "登陆之前允许发消息"),
    BLINDING_BEFORE_LOGIN(
        "blindingBeforeLogin",
        ConfigConstants.Path.SETTINGS_BLINDING_BEFORE_LOGIN,
        false,
        "登陆之前失明效果");

    private static final Map<String, SubCommand> BY_COMMAND = new HashMap<>();

    static {
      for (SubCommand sub : values()) {
        BY_COMMAND.put(sub.command.toLowerCase(Locale.ROOT), sub);
      }
    }

    private final String command;
    private final int minArgs;
    private final Handler handler;
    private final Completer completer;

    SubCommand(String command, int minArgs, Handler handler) {
      this(command, minArgs, handler, Collections::emptyList);
    }

    SubCommand(String command, int minArgs, Handler handler, Completer completer) {
      this.command = command;
      this.minArgs = minArgs;
      this.handler = handler;
      this.completer = completer;
    }

    /** 开关型子命令：翻转布尔配置并回显开关状态。 */
    SubCommand(String command, String configPath, boolean defaultValue, String description) {
      this(command, 1, (sender, args) -> toggle(sender, configPath, defaultValue, description));
    }

    String getCommand() {
      return command;
    }

    /** 参数不足时返回 false，交由 Bukkit 输出用法提示。 */
    boolean execute(CommandSender sender, String[] args) {
      return args.length >= minArgs && handler.execute(sender, args);
    }

    List<String> complete() {
      return completer.complete();
    }

    static SubCommand find(String input) {
      return input == null ? null : BY_COMMAND.get(input.toLowerCase(Locale.ROOT));
    }
  }

  /** 所有管理子命令，由 {@link SubCommand} 派生，TAB 补全与注册表共用同一份数据源。 */
  public static final List<String> SUB_COMMANDS =
      Collections.unmodifiableList(
          Arrays.stream(SubCommand.values())
              .map(SubCommand::getCommand)
              .collect(Collectors.toList()));

  private static BukkitConfigManager config() {
    return BukkitContext.getConfigManager();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 0) {
      return false;
    }
    SubCommand sub = SubCommand.find(args[0]);
    return sub != null && sub.execute(sender, args);
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
      SubCommand sub = SubCommand.find(args[0]);
      if (sub != null) {
        return TabCompleteUtil.filter(sub.complete(), args[1]);
      }
    }
    return Collections.emptyList();
  }

  /** 在线玩家名优先，其后补充数据库中已注册的玩家名。 */
  private static List<String> playerNames() {
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

  // ---- 通用设置助手 ----

  /** 翻转布尔配置并回显开关状态。 */
  private static boolean toggle(
      CommandSender sender, String configPath, boolean defaultValue, String description) {
    try {
      boolean enabled = config().toggle(configPath, defaultValue);
      sender.sendMessage(
          enabled
              ? MessageKey.ADMIN_TOGGLE_ON.get(description)
              : MessageKey.ADMIN_TOGGLE_OFF.get(description));
    } catch (Exception e) {
      sender.sendMessage(MessageKey.ADMIN_SET_FAILED.get(e.getMessage()));
    }
    return true;
  }

  /** 解析并写入单个整数配置，解析失败时提示需要输入数字。 */
  private static boolean setInt(
      CommandSender sender, String[] args, String configPath, IntFunction<String> successMessage) {
    int value;
    try {
      value = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      sender.sendMessage(MessageKey.ADMIN_ENTER_NUMBER.get());
      return true;
    }
    config().set(configPath, value);
    sender.sendMessage(successMessage.apply(value));
    return true;
  }

  /** 解析并写入单个长整数配置，解析失败时提示需要输入数字。 */
  private static boolean setLong(
      CommandSender sender, String[] args, String configPath, LongFunction<String> successMessage) {
    long value;
    try {
      value = Long.parseLong(args[1]);
    } catch (NumberFormatException e) {
      sender.sendMessage(MessageKey.ADMIN_ENTER_NUMBER.get());
      return true;
    }
    config().set(configPath, value);
    sender.sendMessage(successMessage.apply(value));
    return true;
  }

  private static boolean setIdLength(CommandSender sender, String[] args) {
    int min;
    int max;
    try {
      min = Integer.parseInt(args[1]);
      max = Integer.parseInt(args[2]);
    } catch (NumberFormatException e) {
      sender.sendMessage(MessageKey.ADMIN_ENTER_NUMBER.get());
      return true;
    }
    config().set(ConfigConstants.Path.SETTINGS_MIN_LENGTH_ID, min);
    config().set(ConfigConstants.Path.SETTINGS_MAX_LENGTH_ID, max);
    sender.sendMessage(MessageKey.ADMIN_ID_LENGTH_SET.get(min, max));
    return true;
  }

  // ---- Command Whitelist ----

  private static boolean commandWhiteListInfo(CommandSender sender, String[] args) {
    sender.sendMessage(MessageKey.ADMIN_COMMAND_WHITELIST_INFO.get());
    config().getCommandWhiteList().forEach(regex -> sender.sendMessage(regex.toString()));
    return true;
  }

  private static boolean commandWhiteListAdd(CommandSender sender, String[] args) {
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

  private static boolean commandWhiteListDel(CommandSender sender, String[] args) {
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

  private static boolean setSpawnLocation(CommandSender sender, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(MessageKey.CANNOT_USE_FROM_CONSOLE.get());
      return true;
    }
    config().setSpawnLocation(((Player) sender).getLocation());
    sender.sendMessage(MessageKey.SPAWN_LOCATION_SET_MSG.get());
    return true;
  }

  // ---- Reload ----

  private static boolean reload(CommandSender sender, String[] args) {
    config().reload();
    closeOldDatabaseConnection();
    openDatabaseConnection();
    restartCommunicationServer();
    sender.sendMessage(MessageKey.CONFIG_RELOADED_MSG.get());
    return true;
  }

  private static void closeOldDatabaseConnection() {
    try {
      BukkitContext.getSql().closeConnection();
    } catch (Exception e) {
      BukkitContext.getLogger().warning("§c关闭旧数据库连接时出错");
      e.printStackTrace();
    }
  }

  private static void openDatabaseConnection() {
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
  }

  private static void restartCommunicationServer() {
    try {
      CommunicationServer.stopAsync();
    } catch (Exception e) {
      BukkitContext.getLogger().warning("§c停止通信服务时出错");
      e.printStackTrace();
    }
    if (!config().isProxyEnabled()) {
      return;
    }
    try {
      CommunicationServer.startAsync();
    } catch (Exception e) {
      BukkitContext.getLogger().warning("§c启动通信服务时出错");
      e.printStackTrace();
    }
  }

  // ---- Delete Player ----

  private static boolean delPlayer(CommandSender sender, String[] args) {
    String name = args[1];
    LoginPlayer lp = Cache.getIgnoreCase(name);
    if (lp == null) {
      sender.sendMessage(MessageKey.ACCOUNT_NOT_EXISTS.get(name));
      return true;
    }
    delPlayerAsync(sender, lp);
    return true;
  }

  private static void delPlayerAsync(CommandSender sender, LoginPlayer lp) {
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

  private static boolean setPwd(CommandSender sender, String[] args) {
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

  private static void setPwdLookup(CommandSender sender, String name, String pwd) {
    LoginPlayer lp = Cache.getIgnoreCase(name);
    if (lp == null) {
      setPwdRegisterNew(sender, name, pwd);
    } else {
      setPwdUpdateExisting(sender, lp, pwd);
    }
  }

  private static void setPwdRegisterNew(CommandSender sender, String name, String pwd) {
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

  private static void setPwdUpdateExisting(CommandSender sender, LoginPlayer lp, String pwd) {
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

  private static void notifyPlayerPasswordChanged(LoginPlayer lp) {
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
