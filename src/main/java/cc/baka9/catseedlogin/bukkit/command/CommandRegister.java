package cc.baka9.catseedlogin.bukkit.command;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import cc.baka9.catseedlogin.bukkit.CatScheduler;
import cc.baka9.catseedlogin.bukkit.Config;
import cc.baka9.catseedlogin.bukkit.PluginContext;
import cc.baka9.catseedlogin.bukkit.Cache;
import cc.baka9.catseedlogin.bukkit.event.CatSeedPlayerRegisterEvent;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import cc.baka9.catseedlogin.common.model.LoginPlayer;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.common.util.ValidationUtil;

public class CommandRegister implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String lable, String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) return false;
        Player player = (Player) sender;
        String name = sender.getName();

        if (!canRegister(player, name)) {
            return true;
        }
        if (!args[0].equals(args[1])) {
            sender.sendMessage(Config.Language.REGISTER_PASSWORD_CONFIRM_FAIL);
            return true;
        }
        if (ValidationUtil.isPasswordTooSimple(args[0])) {
            sender.sendMessage(Config.Language.COMMON_PASSWORD_SO_SIMPLE);
            return true;
        }
        if (!Cache.isLoaded) return true;

        sender.sendMessage(MessageKey.REGISTERING.get());
        registerPlayerAsync(player, name, args[0]);
        return true;
    }

    private boolean canRegister(Player player, String name) {
        if (Config.Settings.BedrockLoginBypass && LoginPlayerHelper.isFloodgatePlayer(player)) return false;
        if (LoginPlayerHelper.isLogin(name)) {
            player.sendMessage(Config.Language.REGISTER_AFTER_LOGIN_ALREADY);
            return false;
        }
        if (LoginPlayerHelper.isRegister(name)) {
            player.sendMessage(Config.Language.REGISTER_BEFORE_LOGIN_ALREADY);
            return false;
        }
        return true;
    }

    private void registerPlayerAsync(Player player, String name, String password) {
        if (player.getAddress() == null || player.getAddress().getAddress() == null) {
            player.sendMessage(MessageKey.INTERNAL_ERROR.get());
            return;
        }
        String currentIp = player.getAddress().getAddress().getHostAddress();
        boolean isLoopback = player.getAddress().getAddress().isLoopbackAddress();
        CatScheduler.runTaskAsync(() -> {
            try {
                processRegistration(player, name, password, currentIp, isLoopback);
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(MessageKey.INTERNAL_ERROR.get());
            }
        });
    }

    private void processRegistration(Player player, String name, String password, String currentIp, boolean isLoopback) throws Exception {
        List<LoginPlayer> loginPlayersByIp = PluginContext.getSql().getLikeByIp(currentIp);

        if (!isLoopback
                && loginPlayersByIp.size() >= Config.Settings.IpRegisterCountLimit) {
            player.sendMessage(Config.Language.REGISTER_MORE
                    .replace("{count}", String.valueOf(loginPlayersByIp.size()))
                    .replace("{accounts}", String.join(", ",
                            loginPlayersByIp.stream().map(LoginPlayer::getName).toArray(String[]::new))));
            return;
        }

        LoginPlayer lp = new LoginPlayer(name, password);
        lp.crypt();
        PluginContext.getSql().add(lp);
        Cache.refresh(lp.getName());
        LoginPlayerHelper.add(lp);
        CatScheduler.runTask(() -> {
            CatSeedPlayerRegisterEvent event = new CatSeedPlayerRegisterEvent(Bukkit.getPlayer(name));
            Bukkit.getServer().getPluginManager().callEvent(event);
        });
        player.sendMessage(Config.Language.REGISTER_SUCCESS);
        CatScheduler.updateInventory(player);
        LoginPlayerHelper.recordCurrentIP(player, lp);
    }
}