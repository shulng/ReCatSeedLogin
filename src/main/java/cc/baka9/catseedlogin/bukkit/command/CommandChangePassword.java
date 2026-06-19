package cc.baka9.catseedlogin.bukkit.command;

import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import cc.baka9.catseedlogin.bukkit.CatScheduler;
import cc.baka9.catseedlogin.bukkit.Config;
import cc.baka9.catseedlogin.bukkit.PluginContext;
import cc.baka9.catseedlogin.bukkit.Cache;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import cc.baka9.catseedlogin.common.model.LoginPlayer;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.common.util.Crypt;
import cc.baka9.catseedlogin.common.util.ValidationUtil;

public class CommandChangePassword implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String lable, String[] args) {
        if (args.length != 3 || !(sender instanceof Player)) return false;

        Player player = (Player) sender;
        String name = player.getName();

        if (Config.Settings.BedrockLoginBypass && LoginPlayerHelper.isFloodgatePlayer(player)) return true;

        LoginPlayer lp = Cache.getIgnoreCase(name);
        if (lp == null) {
            sender.sendMessage(Config.Language.CHANGEPASSWORD_NOREGISTER);
            return true;
        }
        if (!LoginPlayerHelper.isLogin(name)) {
            sender.sendMessage(Config.Language.CHANGEPASSWORD_NOLOGIN);
            return true;
        }
        if (!Objects.equals(Crypt.encrypt(name, args[0]), lp.getPassword().trim())) {
            sender.sendMessage(Config.Language.CHANGEPASSWORD_OLDPASSWORD_INCORRECT);
            return true;
        }
        if (!args[1].equals(args[2])) {
            sender.sendMessage(Config.Language.CHANGEPASSWORD_PASSWORD_CONFIRM_FAIL);
            return true;
        }
        if (ValidationUtil.isPasswordTooSimple(args[1])) {
            sender.sendMessage(Config.Language.COMMON_PASSWORD_SO_SIMPLE);
            return true;
        }
        if (!Cache.isLoaded) return true;

        sender.sendMessage(MessageKey.CHANGING_PASSWORD.get());
        changePasswordAsync(sender, player, lp, args[1]);
        return true;
    }

    private void changePasswordAsync(CommandSender sender, Player player, LoginPlayer lp, String newPwd) {
        CatScheduler.runTaskAsync(() -> executePasswordChange(sender, player, lp, newPwd));
    }

    private void executePasswordChange(CommandSender sender, Player player, LoginPlayer lp, String newPwd) {
        try {
            LoginPlayer copy = lp.copy();
            copy.setPassword(newPwd);
            copy.crypt();
            PluginContext.getSql().edit(copy);
            Cache.refresh(copy.getName());
            LoginPlayerHelper.remove(lp);
            CatScheduler.runTask(() -> notifyChangeSuccess(sender, player));
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(MessageKey.INTERNAL_ERROR.get());
        }
    }

    private void notifyChangeSuccess(CommandSender sender, Player player) {
        Player online = Bukkit.getPlayer(player.getUniqueId());
        if (online == null || !online.isOnline()) return;

        online.sendMessage(Config.Language.CHANGEPASSWORD_SUCCESS);
        Config.setOfflineLocation(online);
        if (!Config.Settings.CanTpSpawnLocation) return;

        CatScheduler.teleport(online, Config.Settings.SpawnLocation);
        if (PluginContext.isLoadProtocolLib()) {
            LoginPlayerHelper.sendBlankInventoryPacket(online);
        }
    }
}