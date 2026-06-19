package cc.baka9.catseedlogin.bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import cc.baka9.catseedlogin.bukkit.command.*;
import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager;
import cc.baka9.catseedlogin.bukkit.config.BukkitPlatformAdapter;
import cc.baka9.catseedlogin.bukkit.database.*;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.bukkit.task.Task;
import cc.baka9.catseedlogin.common.api.PlatformAdapter;
import cc.baka9.catseedlogin.common.i18n.I18n;
import cn.handyplus.lib.adapter.HandySchedulerUtil;
import space.arim.morepaperlib.MorePaperLib;

public class CatSeedLogin extends JavaPlugin implements Listener {

    public static volatile CatSeedLogin instance;
    public static volatile SQL sql;
    public static volatile boolean loadProtocolLib = false;
    public static volatile MorePaperLib morePaperLib;
    
    private BukkitConfigManager configManager;
    private BukkitPlatformAdapter platformAdapter;

    @Override
    public void onEnable() {
        instance = this;
        morePaperLib = new MorePaperLib(this);
        CatScheduler.init(morePaperLib);
        HandySchedulerUtil.init(this);
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

        getServer().getPluginManager().registerEvents(new Listeners(), this);

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

        if (configManager.isEnable()) {
            Communication.socketServerStartAsync();
        }

        if (Bukkit.getPluginManager().getPlugin("floodgate") != null && configManager.isBedrockLoginBypass()) {
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
        cmd.setTabCompleter((commandSender, command, s, args)
                -> args.length == 1 ? Collections.singletonList("密码") : new ArrayList<>(0));
    }

    private void registerRegisterCommand() {
        PluginCommand cmd = getServer().getPluginCommand("register");
        if (cmd == null) return;
        cmd.setExecutor(new CommandRegister());
        cmd.setTabCompleter((commandSender, command, s, args)
                -> args.length == 1 ? Collections.singletonList("密码 重复密码") : new ArrayList<>(0));
    }

    private void registerChangePasswordCommand() {
        PluginCommand cmd = getServer().getPluginCommand("changepassword");
        if (cmd == null) return;
        cmd.setExecutor(new CommandChangePassword());
        cmd.setTabCompleter((commandSender, command, s, args)
                -> args.length == 1 ? Collections.singletonList("旧密码 新密码 重复新密码") : new ArrayList<>(0));
    }

    private void registerBindEmailCommand() {
        PluginCommand bindemail = getServer().getPluginCommand("bindemail");
        if (bindemail == null) return;
        bindemail.setExecutor(new CommandBindEmail());
        bindemail.setTabCompleter((commandSender, command, s, args) -> {
            if (args.length == 1) {
                return Arrays.asList("set 需要绑定的邮箱", "verify 邮箱验证码");
            }
            if (args.length == 2) {
                if (args[0].equals("set")) {
                    return Collections.singletonList("需要绑定的邮箱");
                }
                if (args[0].equals("verify")) {
                    return Collections.singletonList("邮箱获取的验证码");
                }
            }
            return Collections.emptyList();
        });
    }

    private void registerResetPasswordCommand() {
        PluginCommand resetpassword = getServer().getPluginCommand("resetpassword");
        if (resetpassword == null) return;
        resetpassword.setExecutor(new CommandResetPassword());
        resetpassword.setTabCompleter((commandSender, command, s, args) -> {
            if (args.length == 1) {
                return Arrays.asList("forget", "re 验证码 新密码");
            }
            if (args.length == 2 && "re".equals(args[0])) {
                return Collections.singletonList("验证码 新密码");
            }
            if (args.length == 3 && "re".equals(args[0])) {
                return Collections.singletonList("新密码");
            }
            return Collections.emptyList();
        });
    }

    private void registerCatSeedLoginCommand() {
        PluginCommand cmd = getServer().getPluginCommand("catseedlogin");
        if (cmd == null) return;
        cmd.setExecutor(new CommandCatSeedLogin());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        LoginPlayerHelper.onPlayerQuit(event.getPlayer().getName());
    }

    @Override
    public void onDisable() {
        Task.cancelAll();
        Bukkit.getOnlinePlayers().forEach(p -> {
            if (LoginPlayerHelper.isLogin(p.getName()) && (!p.isDead() || configManager.isDeathStateQuitRecordLocation())) {
                Config.setOfflineLocationSync(p);
            }
        });

        try {
            sql.closeConnection();
        } catch (Exception e) {
            getLogger().warning("关闭数据库连接时出错");
            e.printStackTrace();
        }
        Communication.socketServerStop();
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