package cc.baka9.catseedlogin.bukkit;

import cc.baka9.catseedlogin.common.config.ConfigConstants;
import cc.baka9.catseedlogin.common.config.ConfigHelper;
import cc.baka9.catseedlogin.common.util.ValidationUtil;
import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager;
import cc.baka9.catseedlogin.common.i18n.MessageKey;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Config {
    private static CatSeedLogin plugin;

    public static class MySQL {
        public static volatile boolean Enable;
        public static volatile String Host;
        public static volatile String Port;
        public static volatile String Database;
        public static volatile String User;
        public static volatile String Password;

        public static void load(){
            BukkitConfigManager cm = plugin.getConfigManager();
            Enable = cm.isMySQL();
            Host = cm.getDatabaseHost();
            Port = String.valueOf(cm.getDatabasePort());
            Database = cm.getDatabaseName();
            User = cm.getDatabaseUser();
            Password = cm.getDatabasePassword();
        }
    }

    public static class BungeeCord {
        public static volatile boolean Enable;
        public static volatile String Host;
        public static volatile String Port;
        public static volatile String AuthKey;

        public static void load(){
            BukkitConfigManager cm = plugin.getConfigManager();
            Enable = cm.isEnable();
            Host = cm.getProxyHost();
            Port = String.valueOf(cm.getProxyPort());
            AuthKey = cm.getAuthKey();
        }
    }

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
        public static volatile List<Pattern> CommandWhiteList = new java.util.concurrent.CopyOnWriteArrayList<>();
        public static volatile int AutoKick;
        public static volatile String NamePattern;
        public static volatile boolean DeathStateQuitRecordLocation;
        public static volatile boolean FloodgatePrefixProtect;

        public static void load(){
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
        }

        public static void save(){
            BukkitConfigManager cm = plugin.getConfigManager();
            cm.set(ConfigConstants.Path.SETTINGS_IP_REGISTER_LIMIT, IpRegisterCountLimit);
            cm.set(ConfigConstants.Path.SETTINGS_IP_COUNT_LIMIT, IpCountLimit);
            cm.set(ConfigConstants.Path.SETTINGS_LIMIT_CHINESE_ID, LimitChineseID);
            cm.set(ConfigConstants.Path.BEDROCK_LOGIN_BYPASS, BedrockLoginBypass);
            cm.set(ConfigConstants.Path.SAME_IP_ENABLED, LoginwiththesameIP);
            cm.set(ConfigConstants.Path.EMPTY_BACKPACK, EmptyBackpack);
            cm.set(ConfigConstants.Path.SAME_IP_TIMEOUT, IPTimeout);
            cm.set(ConfigConstants.Path.SETTINGS_MIN_LENGTH_ID, MinLengthID);
            cm.set(ConfigConstants.Path.SETTINGS_MAX_LENGTH_ID, MaxLengthID);
            cm.set(ConfigConstants.Path.SETTINGS_BEFORE_LOGIN_NO_DAMAGE, BeforeLoginNoDamage);
            cm.set(ConfigConstants.Path.SETTINGS_REENTER_INTERVAL, ReenterInterval);
            cm.set(ConfigConstants.Path.SETTINGS_AFTER_LOGIN_BACK, AfterLoginBack);
            cm.set(ConfigConstants.Path.SETTINGS_CAN_TP_SPAWN_LOCATION, CanTpSpawnLocation);
            cm.set(ConfigConstants.Path.SETTINGS_AUTO_KICK, AutoKick);
            cm.set(ConfigConstants.Path.SETTINGS_DEATH_STATE_QUIT_RECORD, DeathStateQuitRecordLocation);
            cm.set(ConfigConstants.Path.BEDROCK_FLOODGATE_PREFIX, FloodgatePrefixProtect);
            cm.set(ConfigConstants.Path.SETTINGS_NAME_PATTERN, NamePattern);
            
            if (CommandWhiteList != null && !CommandWhiteList.isEmpty()) {
                cm.getMainConfig().set(ConfigConstants.Path.SETTINGS_COMMAND_WHITELIST, 
                    CommandWhiteList.stream().map(Pattern::toString).collect(Collectors.toList()));
            }
            
            if (SpawnLocation != null) {
                cm.setSpawnLocation(SpawnLocation);
            }
        }
    }

    public static class Language {
        public static volatile String LOGIN_REQUEST;
        public static volatile String REGISTER_REQUEST;
        public static volatile String LOGIN_NOREGISTER;
        public static volatile String LOGIN_REPEAT;
        public static volatile String LOGIN_SUCCESS;
        public static volatile String LOGIN_FAIL;
        public static volatile String LOGIN_FAIL_IF_FORGET;
        public static volatile String REGISTER_SUCCESS;
        public static volatile String REGISTER_BEFORE_LOGIN_ALREADY;
        public static volatile String REGISTER_AFTER_LOGIN_ALREADY;
        public static volatile String REGISTER_PASSWORD_CONFIRM_FAIL;
        public static volatile String COMMON_PASSWORD_SO_SIMPLE;
        public static volatile String RESETPASSWORD_NOREGISTER;
        public static volatile String RESETPASSWORD_EMAIL_DISABLE;
        public static volatile String RESETPASSWORD_EMAIL_NO_SET;
        public static volatile String RESETPASSWORD_EMAIL_REPEAT_SEND_MESSAGE;
        public static volatile String RESETPASSWORD_EMAIL_SENDING_MESSAGE;
        public static volatile String RESETPASSWORD_EMAIL_SENT_MESSAGE;
        public static volatile String RESETPASSWORD_EMAIL_WARN;
        public static volatile String RESETPASSWORD_SUCCESS;
        public static volatile String RESETPASSWORD_EMAILCODE_INCORRECT;
        public static volatile String RESETPASSWORD_FAIL;
        public static volatile String CHANGEPASSWORD_NOREGISTER;
        public static volatile String CHANGEPASSWORD_NOLOGIN;
        public static volatile String CHANGEPASSWORD_OLDPASSWORD_INCORRECT;
        public static volatile String CHANGEPASSWORD_PASSWORD_CONFIRM_FAIL;
        public static volatile String CHANGEPASSWORD_SUCCESS;
        public static volatile String AUTO_KICK;
        public static volatile String REGISTER_MORE;
        public static volatile String BEDROCK_LOGIN_BYPASS;
        public static volatile String LOGIN_WITH_THE_SAME_IP;

        public static void load(){
            LOGIN_REQUEST = MessageKey.LOGIN_REQUEST.get();
            REGISTER_REQUEST = MessageKey.REGISTER_REQUEST.get();
            LOGIN_NOREGISTER = MessageKey.LOGIN_NOREGISTER.get();
            LOGIN_REPEAT = MessageKey.LOGIN_REPEAT.get();
            LOGIN_SUCCESS = MessageKey.LOGIN_SUCCESS.get();
            LOGIN_FAIL = MessageKey.LOGIN_FAIL.get();
            LOGIN_FAIL_IF_FORGET = MessageKey.LOGIN_FAIL_IF_FORGET.get();
            REGISTER_SUCCESS = MessageKey.REGISTER_SUCCESS.get();
            REGISTER_BEFORE_LOGIN_ALREADY = MessageKey.REGISTER_BEFORE_LOGIN_ALREADY.get();
            REGISTER_AFTER_LOGIN_ALREADY = MessageKey.REGISTER_AFTER_LOGIN_ALREADY.get();
            REGISTER_PASSWORD_CONFIRM_FAIL = MessageKey.REGISTER_PASSWORD_CONFIRM_FAIL.get();
            COMMON_PASSWORD_SO_SIMPLE = MessageKey.COMMON_PASSWORD_SO_SIMPLE.get();
            RESETPASSWORD_NOREGISTER = MessageKey.RESETPASSWORD_NOREGISTER.get();
            RESETPASSWORD_EMAIL_DISABLE = MessageKey.RESETPASSWORD_EMAIL_DISABLE.get();
            RESETPASSWORD_EMAIL_NO_SET = MessageKey.RESETPASSWORD_EMAIL_NO_SET.get();
            RESETPASSWORD_EMAIL_REPEAT_SEND_MESSAGE = MessageKey.RESETPASSWORD_EMAIL_REPEAT_SEND_MESSAGE.get();
            RESETPASSWORD_EMAIL_SENDING_MESSAGE = MessageKey.RESETPASSWORD_EMAIL_SENDING_MESSAGE.get();
            RESETPASSWORD_EMAIL_SENT_MESSAGE = MessageKey.RESETPASSWORD_EMAIL_SENT_MESSAGE.get();
            RESETPASSWORD_EMAIL_WARN = MessageKey.RESETPASSWORD_EMAIL_WARN.get();
            RESETPASSWORD_SUCCESS = MessageKey.RESETPASSWORD_SUCCESS.get();
            RESETPASSWORD_EMAILCODE_INCORRECT = MessageKey.RESETPASSWORD_EMAILCODE_INCORRECT.get();
            RESETPASSWORD_FAIL = MessageKey.RESETPASSWORD_FAIL.get();
            CHANGEPASSWORD_NOREGISTER = MessageKey.CHANGEPASSWORD_NOREGISTER.get();
            CHANGEPASSWORD_NOLOGIN = MessageKey.CHANGEPASSWORD_NOLOGIN.get();
            CHANGEPASSWORD_OLDPASSWORD_INCORRECT = MessageKey.CHANGEPASSWORD_OLDPASSWORD_INCORRECT.get();
            CHANGEPASSWORD_PASSWORD_CONFIRM_FAIL = MessageKey.CHANGEPASSWORD_PASSWORD_CONFIRM_FAIL.get();
            CHANGEPASSWORD_SUCCESS = MessageKey.CHANGEPASSWORD_SUCCESS.get();
            AUTO_KICK = MessageKey.AUTO_KICK.get();
            REGISTER_MORE = MessageKey.REGISTER_MORE.get();
            BEDROCK_LOGIN_BYPASS = MessageKey.BEDROCK_LOGIN_BYPASS.get();
            LOGIN_WITH_THE_SAME_IP = MessageKey.LOGIN_WITH_THE_SAME_IP.get();
        }
    }

    public static class EmailVerify {
        public static volatile boolean Enable;
        public static volatile String EmailAccount;
        public static volatile String EmailPassword;
        public static volatile String EmailSmtpHost;
        public static volatile String EmailSmtpPort;
        public static volatile boolean SSLAuthVerify;
        public static volatile String FromPersonal;

        public static void load(){
            BukkitConfigManager cm = plugin.getConfigManager();
            Enable = cm.isEmailEnable();
            EmailAccount = cm.getEmailAccount();
            EmailPassword = cm.getEmailPassword();
            EmailSmtpHost = cm.getEmailSmtpHost();
            EmailSmtpPort = cm.getEmailSmtpPort();
            SSLAuthVerify = cm.isSSLAuthVerify();
            FromPersonal = cm.getFromPersonal();
        }
    }

    public static void load(){
        plugin = CatSeedLogin.instance;
        BukkitConfigManager cm = plugin.getConfigManager();
        cm.createDefaultConfig("config.yml");
        MySQL.load();
        Settings.load();
        EmailVerify.load();
        Language.load();
        BungeeCord.load();
    }

    public static void save(){
        Settings.save();
    }

    public static void reload(){
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

    private static Location str2Location(String str){
        try {
            String[] locStrs = str.split(":");
            if (locStrs.length < 6) {
                return getDefaultSpawnLocation();
            }
            World world = Bukkit.getWorld(locStrs[0]);
            if (world == null) {
                world = getDefaultWorld();
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
        World world = getDefaultWorld();
        return world != null ? world.getSpawnLocation() : Bukkit.getWorlds().get(0).getSpawnLocation();
    }

    private static String loc2String(Location loc) {
        try {
            return String.format("%s:%.2f:%.2f:%.2f:%.2f:%.2f",
                    loc.getWorld().getName(),
                    loc.getX(),
                    loc.getY(),
                    loc.getZ(),
                    loc.getYaw(),
                    loc.getPitch());
        } catch (Exception e) {
            e.printStackTrace();
            Location defaultLoc = getDefaultWorld().getSpawnLocation();
            return String.format("%s:%.2f:%.2f:%.2f:%.2f:%.2f",
                    defaultLoc.getWorld().getName(),
                    defaultLoc.getX(),
                    defaultLoc.getY(),
                    defaultLoc.getZ(),
                    defaultLoc.getYaw(),
                    defaultLoc.getPitch());
        }
    }

    private static volatile World cachedDefaultWorld;
    private static volatile boolean defaultWorldCached = false;

    private static World getDefaultWorld() {
        if (defaultWorldCached) {
            return cachedDefaultWorld;
        }
        synchronized (Config.class) {
            if (defaultWorldCached) {
                return cachedDefaultWorld;
            }
            cachedDefaultWorld = resolveDefaultWorld();
            defaultWorldCached = true;
            return cachedDefaultWorld;
        }
    }

    private static World resolveDefaultWorld() {
        if (Bukkit.getWorlds().isEmpty()) {
            return null;
        }

        File serverPropertiesFile = new File("server.properties");
        if (!serverPropertiesFile.exists()) {
            return Bukkit.getWorlds().get(0);
        }

        try (java.io.InputStream is = new java.io.BufferedInputStream(java.nio.file.Files.newInputStream(serverPropertiesFile.toPath()))) {
            java.util.Properties properties = new java.util.Properties();
            properties.load(is);
            String worldName = properties.getProperty("level-name");
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                return world;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Bukkit.getWorlds().get(0);
    }
}
