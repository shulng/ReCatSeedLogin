package cc.baka9.catseedlogin.bukkit.command;

import cc.baka9.catseedlogin.bukkit.cache.PlayerCache;
import cc.baka9.catseedlogin.bukkit.config.Config;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.bukkit.platform.BukkitContext;
import cc.baka9.catseedlogin.bukkit.scheduler.CatScheduler;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import cc.baka9.catseedlogin.common.model.LoginPlayer;
import cc.baka9.catseedlogin.common.util.Crypt;
import cc.baka9.catseedlogin.common.util.ValidationUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandChangePassword extends AbstractCommandSupport {
  @Override
  protected boolean onPlayerCommand(Player player, String[] args) {
    if (args.length != 3) return false;

    CommandSender sender = player;
    String name = player.getName();

    LoginPlayer lp = PlayerCache.getIgnoreCase(name);
    if (lp == null) {
      sender.sendMessage(Config.Language.changePasswordNoRegister);
      return true;
    }
    if (!LoginPlayerHelper.isLogin(name)) {
      sender.sendMessage(Config.Language.changePasswordNoLogin);
      return true;
    }
    if (!Crypt.match(name, args[0], lp.getPassword().trim())) {
      sender.sendMessage(Config.Language.changePasswordOldPasswordIncorrect);
      return true;
    }
    if (!args[1].equals(args[2])) {
      sender.sendMessage(Config.Language.changePasswordPasswordConfirmFail);
      return true;
    }
    if (ValidationUtil.isPasswordTooSimple(args[1])) {
      sender.sendMessage(Config.Language.commonPasswordSoSimple);
      return true;
    }
    if (!PlayerCache.isLoaded) return true;

    sender.sendMessage(MessageKey.CHANGING_PASSWORD.get());
    changePasswordAsync(sender, player, lp, args[1]);
    return true;
  }

  private void changePasswordAsync(
      CommandSender sender, Player player, LoginPlayer lp, String newPwd) {
    CatScheduler.runTaskAsync(() -> executePasswordChange(sender, player, lp, newPwd));
  }

  private void executePasswordChange(
      CommandSender sender, Player player, LoginPlayer lp, String newPwd) {
    try {
      LoginPlayerHelper.changePasswordAndPersist(lp, newPwd);
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

    online.sendMessage(Config.Language.changePasswordSuccess);
    Config.setOfflineLocation(online);
    if (!Config.Settings.canTpSpawnLocation) return;

    CatScheduler.teleport(online, Config.Settings.spawnLocation);
    if (BukkitContext.isLoadProtocolLib()) {
      LoginPlayerHelper.sendBlankInventoryPacket(online);
    }
  }
}
