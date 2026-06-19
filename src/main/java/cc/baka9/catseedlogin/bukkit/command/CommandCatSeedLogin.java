package cc.baka9.catseedlogin.bukkit.command;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.baka9.catseedlogin.bukkit.CatScheduler;
import cc.baka9.catseedlogin.bukkit.Communication;
import cc.baka9.catseedlogin.bukkit.Config;
import cc.baka9.catseedlogin.bukkit.PluginContext;
import cc.baka9.catseedlogin.bukkit.Cache;
import cc.baka9.catseedlogin.bukkit.database.MySQL;
import cc.baka9.catseedlogin.bukkit.database.SQLite;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import cc.baka9.catseedlogin.common.model.LoginPlayer;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.common.util.ValidationUtil;

public class CommandCatSeedLogin implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String lable, String[] args) {
        return reload(sender, args)
                || setPwd(sender, args)
                || delPlayer(sender, args)
                || setIpCountLimit(sender, args)
                || limitChineseID(sender, args)
                || bedrockLoginBypass(sender, args)
                || LoginwiththesameIP(sender, args)
                || setIdLength(sender, args)
                || beforeLoginNoDamage(sender, args)
                || setReenterInterval(sender, args)
                || afterLoginBack(sender, args)
                || setSpawnLocation(sender, args)
                || commandWhiteListInfo(sender, args)
                || commandWhiteListAdd(sender, args)
                || commandWhiteListDel(sender, args)
                || canTpSpawnLocation(sender, args)
                || autoKick(sender, args)
                || setIpRegCountLimit(sender, args)
                || deathStateQuitRecordLocation(sender, args);
    }

    // ---- Helper: Boolean Toggle ----

    private static class BoolSetting {
        final BooleanSupplier getter;
        final Consumer<Boolean> setter;
        final String label;

        BoolSetting(BooleanSupplier getter, Consumer<Boolean> setter, String label) {
            this.getter = getter;
            this.setter = setter;
            this.label = label;
        }
    }

    private boolean toggle(CommandSender sender, String[] args, String key, BoolSetting setting) {
        if (args.length == 0 || !args[0].equalsIgnoreCase(key)) return false;
        try {
            setting.setter.accept(!setting.getter.getAsBoolean());
            Config.Settings.save();
            sender.sendMessage(setting.getter.getAsBoolean()
                    ? MessageKey.ADMIN_TOGGLE_ON.get(setting.label)
                    : MessageKey.ADMIN_TOGGLE_OFF.get(setting.label));
        } catch (Exception e) {
            sender.sendMessage(MessageKey.ADMIN_SET_FAILED.get(e.getMessage()));
        }
        return true;
    }

    // ---- Toggle Settings ----

    private boolean deathStateQuitRecordLocation(CommandSender sender, String[] args) {
        return toggle(sender, args, "deathStateQuitRecordLocation",
                new BoolSetting(() -> Config.Settings.DeathStateQuitRecordLocation,
                        v -> Config.Settings.DeathStateQuitRecordLocation = v,
                        "死亡状态退出游戏记录退出位置"));
    }

    private boolean canTpSpawnLocation(CommandSender sender, String[] args) {
        return toggle(sender, args, "canTpSpawnLocation",
                new BoolSetting(() -> Config.Settings.CanTpSpawnLocation,
                        v -> Config.Settings.CanTpSpawnLocation = v,
                        "登录之前强制在登陆地点"));
    }

    private boolean afterLoginBack(CommandSender sender, String[] args) {
        return toggle(sender, args, "afterLoginBack",
                new BoolSetting(() -> Config.Settings.AfterLoginBack,
                        v -> Config.Settings.AfterLoginBack = v,
                        "登陆之后返回下线地点"));
    }

    private boolean beforeLoginNoDamage(CommandSender sender, String[] args) {
        return toggle(sender, args, "beforeLoginNoDamage",
                new BoolSetting(() -> Config.Settings.BeforeLoginNoDamage,
                        v -> Config.Settings.BeforeLoginNoDamage = v,
                        "登陆之前不受到伤害"));
    }

    private boolean limitChineseID(CommandSender sender, String[] args) {
        return toggle(sender, args, "limitChineseID",
                new BoolSetting(() -> Config.Settings.LimitChineseID,
                        v -> Config.Settings.LimitChineseID = v,
                        "限制中文游戏名"));
    }

    private boolean bedrockLoginBypass(CommandSender sender, String[] args) {
        return toggle(sender, args, "bedrockLoginBypass",
                new BoolSetting(() -> Config.Settings.BedrockLoginBypass,
                        v -> Config.Settings.BedrockLoginBypass = v,
                        "基岩版玩家登录跳过"));
    }

    private boolean LoginwiththesameIP(CommandSender sender, String[] args) {
        return toggle(sender, args, "LoginwiththesameIP",
                new BoolSetting(() -> Config.Settings.LoginwiththesameIP,
                        v -> Config.Settings.LoginwiththesameIP = v,
                        "同IP玩家登录跳过"));
    }

    // ---- Number Settings ----

    private boolean autoKick(CommandSender sender, String[] args) {
        if (args.length < 2 || !args[0].equalsIgnoreCase("setAutoKick")) return false;
        try {
            Config.Settings.AutoKick = Integer.parseInt(args[1]);
            Config.Settings.save();
            sender.sendMessage(Config.Settings.AutoKick > 0
                    ? MessageKey.ADMIN_AUTO_KICK_SET.get(Config.Settings.AutoKick)
                    : MessageKey.ADMIN_AUTO_KICK_DISABLED.get());
        } catch (NumberFormatException e) {
            sender.sendMessage(MessageKey.ADMIN_ENTER_NUMBER.get());
        }
        return true;
    }

    private boolean setReenterInterval(CommandSender sender, String[] args) {
        if (args.length < 2 || !args[0].equalsIgnoreCase("setReenterInterval")) return false;
        try {
            Config.Settings.ReenterInterval = Long.parseLong(args[1]);
            Config.Settings.save();
            sender.sendMessage(MessageKey.ADMIN_REENTER_INTERVAL_SET.get(Config.Settings.ReenterInterval));
        } catch (NumberFormatException e) {
            sender.sendMessage(MessageKey.ADMIN_ENTER_NUMBER.get());
        }
        return true;
    }

    private boolean setIdLength(CommandSender sender, String[] args) {
        if (args.length < 3 || !args[0].equalsIgnoreCase("setIdLength")) return false;
        try {
            Config.Settings.MinLengthID = Integer.parseInt(args[1]);
            Config.Settings.MaxLengthID = Integer.parseInt(args[2]);
            Config.Settings.save();
            sender.sendMessage(MessageKey.ADMIN_ID_LENGTH_SET.get(Config.Settings.MinLengthID, Config.Settings.MaxLengthID));
        } catch (NumberFormatException e) {
            sender.sendMessage(MessageKey.ADMIN_ENTER_NUMBER.get());
        }
        return true;
    }

    private boolean setIpCountLimit(CommandSender sender, String[] args) {
        if (args.length < 2 || !args[0].equalsIgnoreCase("setIpCountLimit")) return false;
        try {
            Config.Settings.IpCountLimit = Integer.parseInt(args[1]);
            Config.Settings.save();
            sender.sendMessage(MessageKey.ADMIN_IP_LOGIN_LIMIT_SET.get(Config.Settings.IpCountLimit));
        } catch (NumberFormatException e) {
            sender.sendMessage(MessageKey.ADMIN_ENTER_NUMBER.get());
        }
        return true;
    }

    private boolean setIpRegCountLimit(CommandSender sender, String[] args) {
        if (args.length < 2 || !args[0].equalsIgnoreCase("setIpRegCountLimit")) return false;
        try {
            Config.Settings.IpRegisterCountLimit = Integer.parseInt(args[1]);
            Config.Settings.save();
            sender.sendMessage(MessageKey.ADMIN_IP_REG_LIMIT_SET.get(Config.Settings.IpRegisterCountLimit));
        } catch (NumberFormatException e) {
            sender.sendMessage(MessageKey.ADMIN_ENTER_NUMBER.get());
        }
        return true;
    }

    // ---- Command Whitelist ----

    private boolean commandWhiteListInfo(CommandSender sender, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("commandWhiteListInfo")) return false;
        sender.sendMessage(MessageKey.ADMIN_COMMAND_WHITELIST_INFO.get());
        Config.Settings.CommandWhiteList.forEach(cmdRegex -> sender.sendMessage(cmdRegex.toString()));
        return true;
    }

    private boolean commandWhiteListAdd(CommandSender sender, String[] args) {
        if (args.length < 2 || !args[0].equalsIgnoreCase("commandWhiteListAdd")) return false;
        String regex = joinArgs(args, 1);
        Pattern pattern = Pattern.compile(regex);
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
        if (args.length < 2 || !args[0].equalsIgnoreCase("commandWhiteListDel")) return false;
        String regex = joinArgs(args, 1);
        if (containsRegex(regex)) {
            removeRegex(regex);
            Config.Settings.save();
            sender.sendMessage(MessageKey.ADMIN_COMMAND_WHITELIST_REMOVED.get(regex));
        } else {
            sender.sendMessage(MessageKey.ADMIN_COMMAND_WHITELIST_NOT_EXISTS.get(regex));
        }
        return true;
    }

    private static String joinArgs(String[] args, int from) {
        String[] cmd = new String[args.length - from];
        System.arraycopy(args, from, cmd, 0, cmd.length);
        return String.join(" ", cmd);
    }

    private static boolean containsRegex(String regex) {
        return Config.Settings.CommandWhiteList.stream()
                .map(Pattern::toString).collect(Collectors.toList()).contains(regex);
    }

    private static void removeRegex(String regex) {
        Config.Settings.CommandWhiteList.removeIf(p -> p.toString().equals(regex));
    }

    // ---- Spawn Location ----

    private boolean setSpawnLocation(CommandSender sender, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("setSpawnLocation")) return false;
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

    private boolean reload(CommandSender sender, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) return false;
        Config.reload();
        try {
            PluginContext.getSql().closeConnection();
        } catch (Exception e) {
            PluginContext.getLogger().warning("§c关闭旧数据库连接时出错");
            e.printStackTrace();
        }
        PluginContext.setSql(Config.MySQL.Enable ? new MySQL(PluginContext.getPlugin()) : new SQLite(PluginContext.getPlugin()));
        try {
            PluginContext.getSql().init();
            Cache.refreshAllSync();
        } catch (Exception e) {
            PluginContext.getLogger().warning("§c加载数据库时出错");
            e.printStackTrace();
        }
        try {
            Communication.socketServerStopAsync();
        } catch (Exception e) {
            PluginContext.getLogger().warning("§c停止通信服务时出错");
            e.printStackTrace();
        }
        if (Config.BungeeCord.Enable) {
            try {
                Communication.socketServerStartAsync();
            } catch (Exception e) {
                PluginContext.getLogger().warning("§c启动通信服务时出错");
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
    }

    private static void kickPlayerIfOnline(String name) {
        CatScheduler.runTask(() -> {
            Player p = Bukkit.getPlayerExact(name);
            if (p != null && p.isOnline()) {
                p.kickPlayer(MessageKey.ACCOUNT_DELETED_KICK.get());
            }
        });
    }

    // ---- Set Password ----

    private boolean setPwd(CommandSender sender, String[] args) {
        if (args.length < 3 || !args[0].equalsIgnoreCase("setpwd")) return false;
        String name = args[1], pwd = args[2];
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
            LoginPlayer lp = new LoginPlayer(name, pwd);
            lp.crypt();
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
            LoginPlayer copy = lp.copy();
            copy.setPassword(pwd);
            copy.crypt();
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