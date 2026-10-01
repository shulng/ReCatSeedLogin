package cc.baka9.catseedlogin.bukkit.command;

import cc.baka9.catseedlogin.bukkit.BukkitContext;
import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.database.OfflineLocationStore;
import cc.baka9.catseedlogin.bukkit.event.CatSeedPlayerLoginEvent;
import cc.baka9.catseedlogin.bukkit.scheduler.CatScheduler;
import cc.baka9.catseedlogin.bukkit.session.LoginPlayerHelper;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import cc.baka9.catseedlogin.common.model.LoginPlayer;
import cc.baka9.catseedlogin.common.util.Crypt;
import cc.baka9.catseedlogin.common.util.PasswordHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** {@code /login} 玩家登录指令。 */
public class CommandLogin implements CommandExecutor {

  private static BukkitConfigManager config() {
    return BukkitContext.getConfigManager();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 0 || !(sender instanceof Player)) return false;
    Player player = (Player) sender;
    String name = player.getName();
    if (config().isBedrockLoginBypass() && LoginPlayerHelper.isFloodgatePlayer(player)) {
      return true;
    }
    if (LoginPlayerHelper.isLogin(name)) {
      sender.sendMessage(MessageKey.LOGIN_REPEAT.get());
      return true;
    }
    LoginPlayer lp = Cache.getIgnoreCase(name);
    if (lp == null) {
      sender.sendMessage(MessageKey.LOGIN_NOREGISTER.get());
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
    CatSeedPlayerLoginEvent loginEvent =
        new CatSeedPlayerLoginEvent(player, lp.getEmail(), CatSeedPlayerLoginEvent.Result.SUCCESS);
    Bukkit.getServer().getPluginManager().callEvent(loginEvent);
    player.sendMessage(MessageKey.LOGIN_SUCCESS.get());
    CatScheduler.updateInventory(player);
    LoginPlayerHelper.recordCurrentIP(player, lp);
    if (config().isAfterLoginBack() && config().isCanTpSpawnLocation()) {
      OfflineLocationStore.get(player)
          .ifPresent(location -> CatScheduler.teleport(player, location));
    }
  }

  /** 旧版 SHA-512 密码在登录成功后自动升级为 Argon2id。 */
  private void upgradeToArgon2(LoginPlayer lp, String rawPassword) {
    CatScheduler.runTaskAsync(
        () -> {
          try {
            LoginPlayer copy = PasswordHelper.updatePassword(lp, rawPassword);
            BukkitContext.getSql().edit(copy);
            Cache.refresh(copy.getName());
          } catch (Exception e) {
            BukkitContext.getLogger()
                .warning("Failed to upgrade password hash to Argon2id for " + lp.getName());
          }
        });
  }

  private void handleLoginFail(CommandSender sender, Player player, LoginPlayer lp) {
    sender.sendMessage(MessageKey.LOGIN_FAIL.get());
    CatSeedPlayerLoginEvent loginEvent =
        new CatSeedPlayerLoginEvent(player, lp.getEmail(), CatSeedPlayerLoginEvent.Result.FAIL);
    Bukkit.getServer().getPluginManager().callEvent(loginEvent);
    if (config().isEmailEnable()) {
      sender.sendMessage(MessageKey.LOGIN_FAIL_IF_FORGET.get());
    }
  }
}
