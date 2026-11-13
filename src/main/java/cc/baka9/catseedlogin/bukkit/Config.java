package cc.baka9.catseedlogin.bukkit;

import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager;
import cc.baka9.catseedlogin.bukkit.util.WorldUtil;
import cc.baka9.catseedlogin.common.config.ConfigConstants;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Config {
  private static CatSeedLogin plugin;

  public static class Settings {
    public static volatile int IpRegisterCountLimit;
    public static volatile int IpCountLimit;
    public static volatile Location SpawnLocation;
    public static volatile boolean LimitChineseID;
    public static volatile boolean BedrockLoginBypass;
    public static volatile boolean LoginwiththesameIP;
    public static volatile boolean EmptyBackpack;
    public static volatile int IPTimeout;
    public static volatile int MaxLengthID;
    public static volatile int MinLengthID;
    public static volatile boolean BeforeLoginNoDamage;
    public static volatile long ReenterInterval;
    public static volatile boolean AfterLoginBack;
    public static volatile boolean CanTpSpawnLocation;
    public static volatile List<Pattern> CommandWhiteList = new java.util.ArrayList<>();
    public static volatile int AutoKick;
    public static volatile String NamePattern;
    public static volatile boolean DeathStateQuitRecordLocation;
    public static volatile boolean FloodgatePrefixProtect;
    public static volatile boolean LoopbackLoginBypass;
    public static volatile boolean BeforeLoginAllowChat;
    public static volatile boolean BlindingBeforeLogin;
    public static volatile boolean TwoFactorEnabled;
    public static volatile String TwoFactorIssuer;
    public static volatile int TwoFactorDigits;
    public static volatile int TwoFactorPeriod;
    public static volatile int TwoFactorRecoveryCodeCount;
    public static volatile int TwoFactorMaxAttempts;
    public static volatile int TwoFactorAttemptWindow;
    public static volatile int TwoFactorPendingTimeout;

    public static void load() {
      BukkitConfigManager cm = plugin.getConfigManager();
      IpRegisterCountLimit = cm.getIpRegisterCountLimit();
      IpCountLimit = cm.getIpCountLimit();
      LimitChineseID = cm.isLimitChineseID();
      MinLengthID = cm.getMinLengthID();
      BedrockLoginBypass = cm.isBedrockLoginBypass();
      LoginwiththesameIP = cm.isLoginWithSameIP();
      EmptyBackpack = cm.isEmptyBackpack();
      MaxLengthID = cm.getMaxLengthID();
      BeforeLoginNoDamage = cm.isBeforeLoginNoDamage();
      ReenterInterval = cm.getReenterInterval();
      AfterLoginBack = cm.isAfterLoginBack();
      CanTpSpawnLocation = cm.isCanTpSpawnLocation();
      NamePattern = cm.getNamePattern();
      CommandWhiteList = cm.getCommandWhiteList();
      AutoKick = cm.getAutoKick();
      IPTimeout = cm.getIPTimeout();
      SpawnLocation = cm.getBukkitSpawnLocation();
      DeathStateQuitRecordLocation = cm.isDeathStateQuitRecordLocation();
      FloodgatePrefixProtect = cm.isFloodgatePrefixProtect();
      LoopbackLoginBypass = cm.isLoopbackLoginBypass();
      BeforeLoginAllowChat = cm.isBeforeLoginAllowChat();
      BlindingBeforeLogin = cm.isBlindingBeforeLogin();
      TwoFactorEnabled = cm.isTwoFactorEnabled();
      TwoFactorIssuer = cm.getTwoFactorIssuer();
      TwoFactorDigits = cm.getTwoFactorDigits();
      TwoFactorPeriod = cm.getTwoFactorPeriod();
      TwoFactorRecoveryCodeCount = cm.getTwoFactorRecoveryCodeCount();
      TwoFactorMaxAttempts = cm.getTwoFactorMaxAttempts();
      TwoFactorAttemptWindow = cm.getTwoFactorAttemptWindow();
      TwoFactorPendingTimeout = cm.getTwoFactorPendingTimeout();
    }

    public static void save() {
      BukkitConfigManager cm = plugin.getConfigManager();
      cm.set(ConfigConstants.Path.SETTINGS_LOGIN_IP_REGISTER_LIMIT, IpRegisterCountLimit);
      cm.set(ConfigConstants.Path.SETTINGS_LOGIN_IP_COUNT_LIMIT, IpCountLimit);
      cm.set(ConfigConstants.Path.SETTINGS_ACCOUNT_LIMIT_CHINESE_ID, LimitChineseID);
      cm.set(ConfigConstants.Path.BEDROCK_LOGIN_BYPASS, BedrockLoginBypass);
      cm.set(ConfigConstants.Path.SETTINGS_LOGIN_SAME_IP_ENABLED, LoginwiththesameIP);
      cm.set(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_EMPTY_BACKPACK, EmptyBackpack);
      cm.set(ConfigConstants.Path.SETTINGS_LOGIN_SAME_IP_TIMEOUT, IPTimeout);
      cm.set(ConfigConstants.Path.SETTINGS_ACCOUNT_MIN_LENGTH_ID, MinLengthID);
      cm.set(ConfigConstants.Path.SETTINGS_ACCOUNT_MAX_LENGTH_ID, MaxLengthID);
      cm.set(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_NO_DAMAGE, BeforeLoginNoDamage);
      cm.set(ConfigConstants.Path.SETTINGS_LOGIN_REENTER_INTERVAL, ReenterInterval);
      cm.set(ConfigConstants.Path.SETTINGS_LOGIN_AFTER_LOGIN_BACK, AfterLoginBack);
      cm.set(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_CAN_TP_SPAWN, CanTpSpawnLocation);
      cm.set(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_AUTO_KICK, AutoKick);
      cm.set(
          ConfigConstants.Path.SETTINGS_LOGIN_DEATH_STATE_QUIT_RECORD,
          DeathStateQuitRecordLocation);
      cm.set(ConfigConstants.Path.BEDROCK_FLOODGATE_PREFIX, FloodgatePrefixProtect);
      cm.set(ConfigConstants.Path.SETTINGS_ACCOUNT_NAME_PATTERN, NamePattern);
      cm.set(ConfigConstants.Path.SETTINGS_LOGIN_LOOPBACK_BYPASS, LoopbackLoginBypass);
      cm.set(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_ALLOW_CHAT, BeforeLoginAllowChat);
      cm.set(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_BLINDING, BlindingBeforeLogin);
      cm.set(ConfigConstants.Path.TWO_FACTOR_ENABLED, TwoFactorEnabled);
      cm.set(ConfigConstants.Path.TWO_FACTOR_ISSUER, TwoFactorIssuer);
      cm.set(ConfigConstants.Path.TWO_FACTOR_DIGITS, TwoFactorDigits);
      cm.set(ConfigConstants.Path.TWO_FACTOR_PERIOD, TwoFactorPeriod);
      cm.set(ConfigConstants.Path.TWO_FACTOR_RECOVERY_CODE_COUNT, TwoFactorRecoveryCodeCount);
      cm.set(ConfigConstants.Path.TWO_FACTOR_MAX_ATTEMPTS, TwoFactorMaxAttempts);
      cm.set(ConfigConstants.Path.TWO_FACTOR_ATTEMPT_WINDOW, TwoFactorAttemptWindow);
      cm.set(ConfigConstants.Path.TWO_FACTOR_PENDING_TIMEOUT, TwoFactorPendingTimeout);

      // 空列表也要写回：否则删掉最后一条白名单永远无法落盘
      cm.getMainConfig()
          .set(
              ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_COMMAND_WHITELIST,
              CommandWhiteList == null
                  ? new java.util.ArrayList<String>()
                  : CommandWhiteList.stream().map(Pattern::toString).collect(Collectors.toList()));

      if (SpawnLocation != null) {
        cm.setSpawnLocation(SpawnLocation);
      }

      cm.saveConfig("config.yml");
    }
  }

  public static void load() {
    plugin = CatSeedLogin.instance;
    Settings.load();
  }

  public static Optional<Location> getOfflineLocation(Player player) {
    try {
      String locStr = CatSeedLogin.sql.getLocation(player.getName());
      if (locStr != null && !locStr.isEmpty()) {
        return Optional.of(str2Location(locStr));
      }
    } catch (Exception e) {
      plugin.getLogger().warning("获取玩家离线位置失败: " + player.getName());
      e.printStackTrace();
    }
    return Optional.empty();
  }

  public static void setOfflineLocation(Player player) {
    String locStr = loc2String(player.getLocation());
    plugin.runTaskAsync(() -> {
      try {
        CatSeedLogin.sql.updateLocation(player.getName(), locStr);
      } catch (Exception e) {
        plugin.getLogger().warning("保存玩家离线位置失败: " + player.getName());
        e.printStackTrace();
      }
    });
  }

  public static void setOfflineLocationSync(Player player) {
    String locStr = loc2String(player.getLocation());
    try {
      CatSeedLogin.sql.updateLocation(player.getName(), locStr);
    } catch (Exception e) {
      plugin.getLogger().warning("保存玩家离线位置失败: " + player.getName());
      e.printStackTrace();
    }
  }

  private static Location str2Location(String str) {
    try {
      String[] locStrs = str.split(":");
      if (locStrs.length < 6) {
        return getDefaultSpawnLocation();
      }
      World world = Bukkit.getWorld(locStrs[0]);
      if (world == null) {
        world = WorldUtil.getDefaultWorld(plugin.getLogger());
      }
      if (world == null) {
        return getDefaultSpawnLocation();
      }
      double x = Double.parseDouble(locStrs[1]);
      double y = Double.parseDouble(locStrs[2]);
      double z = Double.parseDouble(locStrs[3]);
      float yaw = Float.parseFloat(locStrs[4]);
      float pitch = Float.parseFloat(locStrs[5]);
      return new Location(world, x, y, z, yaw, pitch);
    } catch (NumberFormatException ignored) {
      return getDefaultSpawnLocation();
    }
  }

  private static Location getDefaultSpawnLocation() {
    World world = WorldUtil.getDefaultWorld(plugin.getLogger());
    return world != null ? world.getSpawnLocation() : Bukkit.getWorlds().get(0).getSpawnLocation();
  }

  private static String loc2String(Location loc) {
    try {
      return String.format(
          "%s:%.2f:%.2f:%.2f:%.2f:%.2f",
          loc.getWorld().getName(),
          loc.getX(),
          loc.getY(),
          loc.getZ(),
          loc.getYaw(),
          loc.getPitch());
    } catch (Exception e) {
      e.printStackTrace();
      Location defaultLoc = WorldUtil.getDefaultWorld(plugin.getLogger()).getSpawnLocation();
      return String.format(
          "%s:%.2f:%.2f:%.2f:%.2f:%.2f",
          defaultLoc.getWorld().getName(),
          defaultLoc.getX(),
          defaultLoc.getY(),
          defaultLoc.getZ(),
          defaultLoc.getYaw(),
          defaultLoc.getPitch());
    }
  }
}
