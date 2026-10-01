package cc.baka9.catseedlogin.bukkit.command;

import cc.baka9.catseedlogin.bukkit.BukkitContext;
import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.database.OfflineLocationStore;
import cc.baka9.catseedlogin.bukkit.scheduler.CatScheduler;
import cc.baka9.catseedlogin.bukkit.session.LoginPlayerHelper;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import cc.baka9.catseedlogin.common.model.LoginPlayer;
import cc.baka9.catseedlogin.common.util.Crypt;
import cc.baka9.catseedlogin.common.util.PasswordHelper;
import cc.baka9.catseedlogin.common.util.ValidationUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** {@code /changepassword} 修改密码指令。 */
public class CommandChangePassword implements CommandExecutor {

  private static BukkitConfigManager config() {
    return BukkitContext.getConfigManager();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length != 3 || !(sender instanceof Player)) return false;

    Player player = (Player) sender;
    String name = player.getName();

    if (config().isBedrockLoginBypass() && LoginPlayerHelper.isFloodgatePlayer(player)) return true;

    LoginPlayer lp = Cache.getIgnoreCase(name);
    if (lp == null) {
      sender.sendMessage(MessageKey.CHANGEPASSWORD_NOREGISTER.get());
      return true;
    }
    if (!LoginPlayerHelper.isLogin(name)) {
      sender.sendMessage(MessageKey.CHANGEPASSWORD_NOLOGIN.get());
      return true;
    }
    if (!Crypt.match(name, args[0], lp.getPassword().trim())) {
      sender.sendMessage(MessageKey.CHANGEPASSWORD_OLDPASSWORD_INCORRECT.get());
      return true;
    }
    if (!args[1].equals(args[2])) {
      sender.sendMessage(MessageKey.CHANGEPASSWORD_PASSWORD_CONFIRM_FAIL.get());
      return true;
    }
    if (ValidationUtil.isPasswordTooSimple(args[1])) {
      sender.sendMessage(MessageKey.COMMON_PASSWORD_SO_SIMPLE.get());
      return true;
    }
    if (!Cache.isLoaded) return true;

    sender.sendMessage(MessageKey.CHANGING_PASSWORD.get());
    CatScheduler.runTaskAsync(() -> executePasswordChange(sender, player, lp, args[1]));
    return true;
  }

  private void executePasswordChange(
      CommandSender sender, Player player, LoginPlayer lp, String newPwd) {
    try {
      LoginPlayer copy = PasswordHelper.updatePassword(lp, newPwd);
      BukkitContext.getSql().edit(copy);
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

    online.sendMessage(MessageKey.CHANGEPASSWORD_SUCCESS.get());
    OfflineLocationStore.saveAsync(online);
    if (!config().isCanTpSpawnLocation()) return;

    CatScheduler.teleport(online, config().getBukkitSpawnLocation());
    if (BukkitContext.isLoadProtocolLib()) {
      LoginPlayerHelper.sendBlankInventoryPacket(online);
    }
  }
}
