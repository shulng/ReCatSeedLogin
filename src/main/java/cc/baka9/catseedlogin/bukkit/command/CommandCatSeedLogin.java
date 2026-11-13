package cc.baka9.catseedlogin.bukkit.command;

import cc.baka9.catseedlogin.bukkit.Cache;
import cc.baka9.catseedlogin.bukkit.CatScheduler;
import cc.baka9.catseedlogin.bukkit.Config;
import cc.baka9.catseedlogin.bukkit.PluginContext;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.bukkit.object.TwoFactorState;
import cc.baka9.catseedlogin.common.api.Permissions;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import cc.baka9.catseedlogin.common.model.LoginPlayer;
import cc.baka9.catseedlogin.common.util.PasswordHelper;
import cc.baka9.catseedlogin.common.util.TabCompleteUtil;
import cc.baka9.catseedlogin.common.util.ValidationUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class CommandCatSeedLogin implements CommandExecutor, TabCompleter {

  public static final String PERMISSION = Permissions.ADMIN;

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
              "blindingBeforeLogin",
              "2faDisable"));

  /** 布尔开关的统一注册表：新增开关只需在这里登记一次，分发与补全自动生效。 */
  private static final Map<String, BoolSetting> BOOLEAN_SETTINGS = new LinkedHashMap<>();

  static {
    register(
        "limitChineseID",
        () -> Config.Settings.LimitChineseID,
        v -> Config.Settings.LimitChineseID = v,
        MessageKey.ADMIN_LABEL_LIMIT_CHINESE_ID);
    register(
        "bedrockLoginBypass",
        () -> Config.Settings.BedrockLoginBypass,
        v -> Config.Settings.BedrockLoginBypass = v,
        MessageKey.ADMIN_LABEL_BEDROCK_LOGIN_BYPASS);
    register(
        "LoginwiththesameIP",
        () -> Config.Settings.LoginwiththesameIP,
        v -> Config.Settings.LoginwiththesameIP = v,
        MessageKey.ADMIN_LABEL_SAME_IP_LOGIN);
    register(
        "beforeLoginNoDamage",
        () -> Config.Settings.BeforeLoginNoDamage,
        v -> Config.Settings.BeforeLoginNoDamage = v,
        MessageKey.ADMIN_LABEL_BEFORE_LOGIN_NO_DAMAGE);
    register(
        "afterLoginBack",
        () -> Config.Settings.AfterLoginBack,
        v -> Config.Settings.AfterLoginBack = v,
        MessageKey.ADMIN_LABEL_AFTER_LOGIN_BACK);
    register(
        "canTpSpawnLocation",
        () -> Config.Settings.CanTpSpawnLocation,
        v -> Config.Settings.CanTpSpawnLocation = v,
        MessageKey.ADMIN_LABEL_CAN_TP_SPAWN);
    register(
        "deathStateQuitRecordLocation",
        () -> Config.Settings.DeathStateQuitRecordLocation,
        v -> Config.Settings.DeathStateQuitRecordLocation = v,
        MessageKey.ADMIN_LABEL_DEATH_STATE_QUIT_RECORD);
    register(
        "loopbackLoginBypass",
        () -> Config.Settings.LoopbackLoginBypass,
        v -> Config.Settings.LoopbackLoginBypass = v,
        MessageKey.ADMIN_LABEL_LOOPBACK_LOGIN_BYPASS);
    register(
        "beforeLoginAllowChat",
        () -> Config.Settings.BeforeLoginAllowChat,
        v -> Config.Settings.BeforeLoginAllowChat = v,
        MessageKey.ADMIN_LABEL_BEFORE_LOGIN_ALLOW_CHAT);
    register(
        "blindingBeforeLogin",
        () -> Config.Settings.BlindingBeforeLogin,
        v -> Config.Settings.BlindingBeforeLogin = v,
        MessageKey.ADMIN_LABEL_BLINDING_BEFORE_LOGIN);
  }

  private static void register(
      String key, BooleanSupplier getter, Consumer<Boolean> setter, MessageKey label) {
    BOOLEAN_SETTINGS.put(key.toLowerCase(Locale.ROOT), new BoolSetting(getter, setter, label));
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!sender.hasPermission(PERMISSION)) {
      sender.sendMessage(MessageKey.NO_PERMISSION.get());
      return true;
    }
    if (args.length == 0) return false;
    switch (args[0].toLowerCase(Locale.ROOT)) {
      case "reload":
        return reload(sender);
      case "setpwd":
        return setPwd(sender, args);
      case "delplayer":
        return delPlayer(sender, args);
      case "2fadisable":
        return twoFactorDisable(sender, args);
      case "setipcountlimit":
        return setIpCountLimit(sender, args);
      case "setipregcountlimit":
        return setIpRegCountLimit(sender, args);
      case "setidlength":
        return setIdLength(sender, args);
      case "setreenterinterval":
        return setReenterInterval(sender, args);
      case "setautokick":
        return setAutoKick(sender, args);
      case "setspawnlocation":
        return setSpawnLocation(sender);
      case "commandwhitelistinfo":
        return commandWhiteListInfo(sender);
      case "commandwhitelistadd":
        return commandWhiteListAdd(sender, args);
      case "commandwhitelistdel":
        return commandWhiteListDel(sender, args);
      default:
        return toggle(sender, args[0]);
    }
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
      String sub = args[0].toLowerCase(Locale.ROOT);
      if ("delplayer".equals(sub) || "setpwd".equals(sub) || "2fadisable".equals(sub)) {
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
          .forEach(lp -> {
            if (lp != null && lp.getName() != null) {
              names.add(lp.getName());
            }
          });
    }
    return new ArrayList<>(names);
  }

  private static List<String> commandWhiteListRegexes() {
    List<Pattern> whiteList = Config.Settings.CommandWhiteList;
    if (whiteList == null || whiteList.isEmpty()) {
      return Collections.emptyList();
    }
    List<String> regexes = new ArrayList<>(whiteList.size());
    whiteList.forEach(pattern -> regexes.add(pattern.toString()));
    return regexes;
  }

  // ---- Toggle Settings ----

  private static class BoolSetting {
    final BooleanSupplier getter;
    final Consumer<Boolean> setter;
    final MessageKey label;

    BoolSetting(BooleanSupplier getter, Consumer<Boolean> setter, MessageKey label) {
      this.getter = getter;
      this.setter = setter;
      this.label = label;
    }
  }

  private boolean toggle(CommandSender sender, String key) {
    BoolSetting setting = BOOLEAN_SETTINGS.get(key.toLowerCase(Locale.ROOT));
    if (setting == null) return false;
    try {
      setting.setter.accept(!setting.getter.getAsBoolean());
      Config.Settings.save();
      String label = setting.label.get();
      sender.sendMessage(
          setting.getter.getAsBoolean()
              ? MessageKey.ADMIN_TOGGLE_ON.get(label)
              : MessageKey.ADMIN_TOGGLE_OFF.get(label));
    } catch (Exception e) {
      sender.sendMessage(MessageKey.ADMIN_SET_FAILED.get(e.getMessage()));
    }
    return true;
  }

  // ---- Number Settings ----

  private Integer parseNumber(CommandSender sender, String raw) {
    try {
      return Integer.valueOf(raw);
    } catch (NumberFormatException e) {
      sender.sendMessage(MessageKey.ADMIN_ENTER_NUMBER.get());
      return null;
    }
  }

  private boolean setAutoKick(CommandSender sender, String[] args) {
    if (args.length < 2) return false;
    Integer value = parseNumber(sender, args[1]);
    if (value == null) return true;
    Config.Settings.AutoKick = value;
    Config.Settings.save();
    sender.sendMessage(
        value > 0
            ? MessageKey.ADMIN_AUTO_KICK_SET.get(value)
            : MessageKey.ADMIN_AUTO_KICK_DISABLED.get());
    return true;
  }

  private boolean setReenterInterval(CommandSender sender, String[] args) {
    if (args.length < 2) return false;
    long value;
    try {
      value = Long.parseLong(args[1]);
    } catch (NumberFormatException e) {
      sender.sendMessage(MessageKey.ADMIN_ENTER_NUMBER.get());
      return true;
    }
    Config.Settings.ReenterInterval = value;
    Config.Settings.save();
    sender.sendMessage(MessageKey.ADMIN_REENTER_INTERVAL_SET.get(value));
    return true;
  }

  private boolean setIdLength(CommandSender sender, String[] args) {
    if (args.length < 3) return false;
    Integer min = parseNumber(sender, args[1]);
    if (min == null) return true;
    Integer max = parseNumber(sender, args[2]);
    if (max == null) return true;
    Config.Settings.MinLengthID = min;
    Config.Settings.MaxLengthID = max;
    Config.Settings.save();
    sender.sendMessage(MessageKey.ADMIN_ID_LENGTH_SET.get(min, max));
    return true;
  }

  private boolean setIpCountLimit(CommandSender sender, String[] args) {
    if (args.length < 2) return false;
    Integer value = parseNumber(sender, args[1]);
    if (value == null) return true;
    Config.Settings.IpCountLimit = value;
    Config.Settings.save();
    sender.sendMessage(MessageKey.ADMIN_IP_LOGIN_LIMIT_SET.get(value));
    return true;
  }

  private boolean setIpRegCountLimit(CommandSender sender, String[] args) {
    if (args.length < 2) return false;
    Integer value = parseNumber(sender, args[1]);
    if (value == null) return true;
    Config.Settings.IpRegisterCountLimit = value;
    Config.Settings.save();
    sender.sendMessage(MessageKey.ADMIN_IP_REG_LIMIT_SET.get(value));
    return true;
  }

  // ---- Command Whitelist ----

  private boolean commandWhiteListInfo(CommandSender sender) {
    sender.sendMessage(MessageKey.ADMIN_COMMAND_WHITELIST_INFO.get());
    Config.Settings.CommandWhiteList.forEach(cmdRegex -> sender.sendMessage(cmdRegex.toString()));
    return true;
  }

  private boolean commandWhiteListAdd(CommandSender sender, String[] args) {
    if (args.length < 2) return false;
    String regex = joinArgs(args, 1);
    Pattern pattern;
    try {
      pattern = Pattern.compile(regex);
    } catch (Exception e) {
      sender.sendMessage(MessageKey.ADMIN_SET_FAILED.get(e.getMessage()));
      return true;
    }
    if (containsRegex(regex)) {
      sender.sendMessage(MessageKey.ADMIN_COMMAND_WHITELIST_ALREADY_EXISTS.get(regex));
    } else {
      Config.Settings.CommandWhiteList.add(pattern);
      Config.Settings.save();
      sender.sendMessage(MessageKey.ADMIN_COMMAND_WHITELIST_ADDED.get(regex));
    }
    return true;
  }

  private boolean commandWhiteListDel(CommandSender sender, String[] args) {
    if (args.length < 2) return false;
    String regex = joinArgs(args, 1);
    if (containsRegex(regex)) {
      Config.Settings.CommandWhiteList.removeIf(p -> p.toString().equals(regex));
      Config.Settings.save();
      sender.sendMessage(MessageKey.ADMIN_COMMAND_WHITELIST_REMOVED.get(regex));
    } else {
      sender.sendMessage(MessageKey.ADMIN_COMMAND_WHITELIST_NOT_EXISTS.get(regex));
    }
    return true;
  }

  private static String joinArgs(String[] args, int from) {
    return String.join(" ", Arrays.asList(args).subList(from, args.length));
  }

  private static boolean containsRegex(String regex) {
    return Config.Settings.CommandWhiteList.stream().anyMatch(p -> p.toString().equals(regex));
  }

  // ---- Spawn Location ----

  private boolean setSpawnLocation(CommandSender sender) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(MessageKey.CANNOT_USE_FROM_CONSOLE.get());
      return true;
    }
    Config.Settings.SpawnLocation = ((Player) sender).getLocation();
    Config.Settings.save();
    sender.sendMessage(MessageKey.SPAWN_LOCATION_SET_MSG.get());
    return true;
  }

  // ---- Reload ----

  private boolean reload(CommandSender sender) {
    try {
      PluginContext.getPlugin().reloadAll();
      sender.sendMessage(MessageKey.CONFIG_RELOADED.get());
    } catch (Exception e) {
      sender.sendMessage(MessageKey.INTERNAL_ERROR.get());
      PluginContext.getLogger().warning("重载插件时出错");
      e.printStackTrace();
    }
    return true;
  }

  // ---- Delete Player ----

  private boolean delPlayer(CommandSender sender, String[] args) {
    if (args.length < 2) return false;
    String name = args[1];
    LoginPlayer lp = Cache.getIgnoreCase(name);
    if (lp == null) {
      sender.sendMessage(MessageKey.ACCOUNT_NOT_EXISTS.get(name));
      return true;
    }
    CatScheduler.runTaskAsync(() -> {
      try {
        PluginContext.getSql().del(lp.getName());
        Cache.refresh(lp.getName());
        LoginPlayerHelper.remove(lp);
        sender.sendMessage(MessageKey.ACCOUNT_DELETED.get(lp.getName()));
        kickPlayerIfOnline(lp.getName());
      } catch (Exception e) {
        sender.sendMessage(MessageKey.DATABASE_ERROR.get());
        e.printStackTrace();
      }
    });
    return true;
  }

  private static void kickPlayerIfOnline(String name) {
    CatScheduler.runTask(() -> {
      Player p = Bukkit.getPlayerExact(name);
      if (p != null && p.isOnline()) {
        p.kickPlayer(MessageKey.ACCOUNT_DELETED_KICK.get());
      }
    });
  }

  // ---- Two Factor Disable ----

  /**
   * 管理员兜底：玩家验证器和恢复码全丢时，强制关闭其两步验证。
   *
   * <p>这是绕过玩家本人验证的唯一入口，因此挂在管理指令下、复用现有的管理权限节点。
   */
  private boolean twoFactorDisable(CommandSender sender, String[] args) {
    if (args.length < 2) return false;
    String name = args[1];
    LoginPlayer lp = Cache.getIgnoreCase(name);
    if (lp == null) {
      sender.sendMessage(MessageKey.ACCOUNT_NOT_EXISTS.get(name));
      return true;
    }
    if (!lp.isTotpEnabled()) {
      sender.sendMessage(MessageKey.ADMIN_2FA_NOT_ENABLED.get(name));
      return true;
    }
    String accountName = lp.getName();
    CatScheduler.runTaskAsync(() -> {
      try {
        PluginContext.getSql().updateTotpSecret(accountName, null);
        PluginContext.getSql().updateTotpEnabled(accountName, false);
        PluginContext.getSql().invalidateRecoveryCodes(accountName);
        Cache.refresh(accountName);
        TwoFactorState.removePendingLogin(accountName);
        CatScheduler.runTask(
            () -> {
              sender.sendMessage(MessageKey.ADMIN_2FA_DISABLED.get(accountName));
              Player target = Bukkit.getPlayerExact(accountName);
              if (target != null && target.isOnline()) {
                target.sendMessage(MessageKey.TWO_FACTOR_DISABLED_BY_ADMIN.get());
              }
            });
      } catch (Exception e) {
        CatScheduler.runTask(() -> sender.sendMessage(MessageKey.DATABASE_ERROR.get()));
        e.printStackTrace();
      }
    });
    return true;
  }

  // ---- Set Password ----

  private boolean setPwd(CommandSender sender, String[] args) {
    if (args.length < 3) return false;
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
      PluginContext.getSql().add(lp);
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
      PluginContext.getSql().edit(copy);
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
    CatScheduler.runTask(() -> {
      Player p = Bukkit.getPlayer(lp.getName());
      if (p == null || !p.isOnline()) return;
      p.sendMessage(MessageKey.PASSWORD_RESET_BY_ADMIN.get());
      if (!Config.Settings.CanTpSpawnLocation) return;
      CatScheduler.teleport(p, Config.Settings.SpawnLocation);
      if (PluginContext.isLoadProtocolLib()) {
        LoginPlayerHelper.sendBlankInventoryPacket(p);
      }
    });
  }
}
