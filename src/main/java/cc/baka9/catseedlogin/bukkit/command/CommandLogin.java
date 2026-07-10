package cc.baka9.catseedlogin.bukkit.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import cc.baka9.catseedlogin.bukkit.CatScheduler;
import cc.baka9.catseedlogin.bukkit.Config;
import cc.baka9.catseedlogin.bukkit.Cache;
import cc.baka9.catseedlogin.bukkit.event.CatSeedPlayerLoginEvent;
import cc.baka9.catseedlogin.common.model.LoginPlayer;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.common.util.Crypt;
import cc.baka9.catseedlogin.bukkit.PluginContext;

public class CommandLogin implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String lable, String[] args) {
        if (args.length == 0 || !(sender instanceof Player)) return false;
        Player player = (Player) sender;
        String name = player.getName();
        if (Config.Settings.BedrockLoginBypass && LoginPlayerHelper.isFloodgatePlayer(player)) return true;
        if (LoginPlayerHelper.isLogin(name)) {
            sender.sendMessage(Config.Language.LOGIN_REPEAT);
            return true;
        }
        LoginPlayer lp = Cache.getIgnoreCase(name);
        if (lp == null) {
            sender.sendMessage(Config.Language.LOGIN_NOREGISTER);
            return true;
        }
        if (!Crypt.match(name, args[0], lp.getPassword().trim())) {
            handleLoginFail(sender, player, lp);
            return true;
        }
        handleLoginSuccess(player, lp);
        if (!Crypt.isArgon2(lp.getPassword().trim())) {
            upgradeToArgon2(lp, args[0]);
        }
        return true;
    }

    private void handleLoginSuccess(Player player, LoginPlayer lp) {
        LoginPlayerHelper.add(lp);
        CatSeedPlayerLoginEvent loginEvent = new CatSeedPlayerLoginEvent(player, lp.getEmail(), CatSeedPlayerLoginEvent.Result.SUCCESS);
        Bukkit.getServer().getPluginManager().callEvent(loginEvent);
        player.sendMessage(Config.Language.LOGIN_SUCCESS);
        CatScheduler.updateInventory(player);
        LoginPlayerHelper.recordCurrentIP(player, lp);
        if (Config.Settings.AfterLoginBack && Config.Settings.CanTpSpawnLocation) {
            Config.getOfflineLocation(player).ifPresent(location -> CatScheduler.teleport(player, location));
        }
    }

    private void upgradeToArgon2(LoginPlayer lp, String rawPassword) {
        CatScheduler.runTaskAsync(() -> {
            try {
                LoginPlayer copy = lp.copy();
                copy.setPassword(rawPassword);
                copy.crypt();
                PluginContext.getSql().edit(copy);
                Cache.refresh(copy.getName());
            } catch (Exception e) {
                PluginContext.getLogger().warning("Failed to upgrade password hash to Argon2id for " + lp.getName());
            }
        });
    }

    private void handleLoginFail(CommandSender sender, Player player, LoginPlayer lp) {
        sender.sendMessage(Config.Language.LOGIN_FAIL);
        CatSeedPlayerLoginEvent loginEvent = new CatSeedPlayerLoginEvent(player, lp.getEmail(), CatSeedPlayerLoginEvent.Result.FAIL);
        Bukkit.getServer().getPluginManager().callEvent(loginEvent);
        if (Config.EmailVerify.Enable) {
            sender.sendMessage(Config.Language.LOGIN_FAIL_IF_FORGET);
        }
    }
}
