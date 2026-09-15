package cc.baka9.catseedlogin.bukkit.config;

import static cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager.getLocation;

import cc.baka9.catseedlogin.bukkit.CatSeedLogin;

import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager;
import cc.baka9.catseedlogin.bukkit.util.WorldUtil;
import cc.baka9.catseedlogin.common.config.ConfigConstants;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Config {
  private static CatSeedLogin plugin;

  public static class MySQL {
    public static volatile boolean enable;
    public static volatile String host;
    public static volatile String port;
    public static volatile String database;
    public static volatile String user;
    public static volatile String password;

    public static void load() {
      BukkitConfigManager cm = plugin.getConfigManager();
      enable = cm.isMySQL();
      host = cm.getDatabaseHost();
      port = String.valueOf(cm.getDatabasePort());
      database = cm.getDatabaseName();
      user = cm.getDatabaseUser();
      password = cm.getDatabasePassword();
    }
  }

  public static class BungeeCord {
    public static volatile boolean enable;
    public static volatile String host;
    public static volatile String port;
    public static volatile String authKey;

    public static void load() {
      BukkitConfigManager cm = plugin.getConfigManager();
      enable = cm.isEnable();
      host = cm.getProxyHost();
      port = String.valueOf(cm.getProxyPort());
      authKey = cm.getAuthKey();
    }
  }

  public static class Settings {
    public static volatile int ipRegisterCountLimit;
    public static volatile int ipCountLimit;
    public static volatile Location spawnLocation;
    public static volatile boolean limitChineseId;
    public static volatile boolean bedrockLoginBypass;
    public static volatile boolean loginWithSameIp;
    public static volatile boolean emptyBackpack;
    public static volatile int ipTimeout;
    public static volatile int maxLengthId;
    public static volatile int minLengthId;
    public static volatile boolean beforeLoginNoDamage;
    public static volatile long reenterInterval;
    public static volatile boolean afterLoginBack;
    public static volatile boolean canTpSpawnLocation;
    public static volatile List<Pattern> commandWhiteList = new java.util.ArrayList<>();
    public static volatile int autoKick;
    public static volatile String namePattern;
    public static volatile boolean deathStateQuitRecordLocation;
    public static volatile boolean floodgatePrefixProtect;

    public static void load() {
      BukkitConfigManager cm = plugin.getConfigManager();
      ipRegisterCountLimit = cm.getIpRegisterCountLimit();
      ipCountLimit = cm.getIpCountLimit();
      limitChineseId = cm.isLimitChineseID();
      minLengthId = cm.getMinLengthID();
      bedrockLoginBypass = cm.isBedrockLoginBypass();
      loginWithSameIp = cm.isLoginWithSameIP();
      emptyBackpack = cm.isEmptyBackpack();
      maxLengthId = cm.getMaxLengthID();
      beforeLoginNoDamage = cm.isBeforeLoginNoDamage();
      reenterInterval = cm.getReenterInterval();
      afterLoginBack = cm.isAfterLoginBack();
      canTpSpawnLocation = cm.isCanTpSpawnLocation();
      namePattern = cm.getNamePattern();
      commandWhiteList = cm.getCommandWhiteList();
      autoKick = cm.getAutoKick();
      ipTimeout = cm.getIPTimeout();
      spawnLocation = cm.getBukkitSpawnLocation();
      deathStateQuitRecordLocation = cm.isDeathStateQuitRecordLocation();
      floodgatePrefixProtect = cm.isFloodgatePrefixProtect();
    }

    public static void save() {
      BukkitConfigManager cm = plugin.getConfigManager();
      cm.set(ConfigConstants.Path.SETTINGS_IP_REGISTER_LIMIT, ipRegisterCountLimit);
      cm.set(ConfigConstants.Path.SETTINGS_IP_COUNT_LIMIT, ipCountLimit);
      cm.set(ConfigConstants.Path.SETTINGS_LIMIT_CHINESE_ID, limitChineseId);
      cm.set(ConfigConstants.Path.BEDROCK_LOGIN_BYPASS, bedrockLoginBypass);
      cm.set(ConfigConstants.Path.SAME_IP_ENABLED, loginWithSameIp);
      cm.set(ConfigConstants.Path.EMPTY_BACKPACK, emptyBackpack);
      cm.set(ConfigConstants.Path.SAME_IP_TIMEOUT, ipTimeout);
      cm.set(ConfigConstants.Path.SETTINGS_MIN_LENGTH_ID, minLengthId);
      cm.set(ConfigConstants.Path.SETTINGS_MAX_LENGTH_ID, maxLengthId);
      cm.set(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_NO_DAMAGE, beforeLoginNoDamage);
      cm.set(ConfigConstants.Path.SETTINGS_REENTER_INTERVAL, reenterInterval);
      cm.set(ConfigConstants.Path.SETTINGS_AFTER_LOGIN_BACK, afterLoginBack);
      cm.set(ConfigConstants.Path.SETTINGS_CAN_TP_SPAWN_LOCATION, canTpSpawnLocation);
      cm.set(ConfigConstants.Path.SETTINGS_AUTO_KICK, autoKick);
      cm.set(ConfigConstants.Path.SETTINGS_DEATH_STATE_QUIT_RECORD, deathStateQuitRecordLocation);
      cm.set(ConfigConstants.Path.BEDROCK_FLOODGATE_PREFIX, floodgatePrefixProtect);
      cm.set(ConfigConstants.Path.SETTINGS_NAME_PATTERN, namePattern);

      if (commandWhiteList != null && !commandWhiteList.isEmpty()) {
        cm.getMainConfig()
            .set(
                ConfigConstants.Path.SETTINGS_COMMAND_WHITELIST,
                commandWhiteList.stream().map(Pattern::toString).collect(Collectors.toList()));
      }

      if (spawnLocation != null) {
        cm.setSpawnLocation(spawnLocation);
      }
    }
  }

  public static class Language {
    public static String loginRequest;
    public static String registerRequest;
    public static String loginNoRegister;
    public static String loginRepeat;
    public static String loginSuccess;
    public static String loginFail;
    public static String loginFailIfForget;
    public static String registerSuccess;
    public static String registerBeforeLoginAlready;
    public static String registerAfterLoginAlready;
    public static String registerPasswordConfirmFail;
    public static String commonPasswordSoSimple;
    public static String resetPasswordNoRegister;
    public static String resetPasswordEmailDisable;
    public static String resetPasswordEmailNoSet;
    public static String resetPasswordEmailRepeatSendMessage;
    public static String resetPasswordEmailSendingMessage;
    public static String resetPasswordEmailSentMessage;
    public static String resetPasswordEmailWarn;
    public static String resetPasswordSuccess;
    public static String resetPasswordEmailCodeIncorrect;
    public static String resetPasswordFail;
    public static String changePasswordNoRegister;
    public static String changePasswordNoLogin;
    public static String changePasswordOldPasswordIncorrect;
    public static String changePasswordPasswordConfirmFail;
    public static String changePasswordSuccess;
    public static String autoKick;
    public static String registerMore;
    public static String bedrockLoginBypass;
    public static String loginWithTheSameIp;

    public static void load() {
      loginRequest = MessageKey.LOGIN_REQUEST.get();
      registerRequest = MessageKey.REGISTER_REQUEST.get();
      loginNoRegister = MessageKey.LOGIN_NOREGISTER.get();
      loginRepeat = MessageKey.LOGIN_REPEAT.get();
      loginSuccess = MessageKey.LOGIN_SUCCESS.get();
      loginFail = MessageKey.LOGIN_FAIL.get();
      loginFailIfForget = MessageKey.LOGIN_FAIL_IF_FORGET.get();
      registerSuccess = MessageKey.REGISTER_SUCCESS.get();
      registerBeforeLoginAlready = MessageKey.REGISTER_BEFORE_LOGIN_ALREADY.get();
      registerAfterLoginAlready = MessageKey.REGISTER_AFTER_LOGIN_ALREADY.get();
      registerPasswordConfirmFail = MessageKey.REGISTER_PASSWORD_CONFIRM_FAIL.get();
      commonPasswordSoSimple = MessageKey.COMMON_PASSWORD_SO_SIMPLE.get();
      resetPasswordNoRegister = MessageKey.RESETPASSWORD_NOREGISTER.get();
      resetPasswordEmailDisable = MessageKey.RESETPASSWORD_EMAIL_DISABLE.get();
      resetPasswordEmailNoSet = MessageKey.RESETPASSWORD_EMAIL_NO_SET.get();
      RESETPASSWORD_EMAIL_REPEAT_SEND_MESSAGE =
          MessageKey.RESETPASSWORD_EMAIL_REPEAT_SEND_MESSAGE.get();
      resetPasswordEmailSendingMessage = MessageKey.RESETPASSWORD_EMAIL_SENDING_MESSAGE.get();
      resetPasswordEmailSentMessage = MessageKey.RESETPASSWORD_EMAIL_SENT_MESSAGE.get();
      resetPasswordEmailWarn = MessageKey.RESETPASSWORD_EMAIL_WARN.get();
      resetPasswordSuccess = MessageKey.RESETPASSWORD_SUCCESS.get();
      resetPasswordEmailCodeIncorrect = MessageKey.RESETPASSWORD_EMAILCODE_INCORRECT.get();
      resetPasswordFail = MessageKey.RESETPASSWORD_FAIL.get();
      changePasswordNoRegister = MessageKey.CHANGEPASSWORD_NOREGISTER.get();
      changePasswordNoLogin = MessageKey.CHANGEPASSWORD_NOLOGIN.get();
      changePasswordOldPasswordIncorrect = MessageKey.CHANGEPASSWORD_OLDPASSWORD_INCORRECT.get();
      changePasswordPasswordConfirmFail = MessageKey.CHANGEPASSWORD_PASSWORD_CONFIRM_FAIL.get();
      changePasswordSuccess = MessageKey.CHANGEPASSWORD_SUCCESS.get();
      autoKick = MessageKey.AUTO_KICK.get();
      registerMore = MessageKey.REGISTER_MORE.get();
      bedrockLoginBypass = MessageKey.BEDROCK_LOGIN_BYPASS.get();
      loginWithTheSameIp = MessageKey.LOGIN_WITH_THE_SAME_IP.get();
    }
  }

  public static class EmailVerify {
    public static volatile boolean enable;
    public static volatile String emailAccount;
    public static volatile String emailPassword;
    public static volatile String emailSmtpHost;
    public static volatile String emailSmtpPort;
    public static volatile boolean sslAuthVerify;
    public static volatile String fromPersonal;

    public static void load() {
      BukkitConfigManager cm = plugin.getConfigManager();
      enable = cm.isEmailEnable();
      emailAccount = cm.getEmailAccount();
      emailPassword = cm.getEmailPassword();
      emailSmtpHost = cm.getEmailSmtpHost();
      emailSmtpPort = cm.getEmailSmtpPort();
      sslAuthVerify = cm.isSSLAuthVerify();
      fromPersonal = cm.getFromPersonal();
    }
  }

  public static void load() {
    plugin = CatSeedLogin.instance;
    BukkitConfigManager cm = plugin.getConfigManager();
    cm.createDefaultConfig("config.yml");
    MySQL.load();
    Settings.load();
    EmailVerify.load();
    Language.load();
    BungeeCord.load();
  }

  public static void save() {
    Settings.save();
  }

  public static void reload() {
    plugin.getConfigManager().reload();
    load();
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
    plugin.runTaskAsync(
        () -> {
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
      return getLocation(locStrs, world);
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
      Location defaultLoc =
          Objects.requireNonNull(WorldUtil.getDefaultWorld(plugin.getLogger())).getSpawnLocation();
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
