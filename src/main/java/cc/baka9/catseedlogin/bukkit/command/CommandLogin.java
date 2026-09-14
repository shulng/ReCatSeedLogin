package cc.baka9.catseedlogin.bukkit.command;

import cc.baka9.catseedlogin.bukkit.cache.PlayerCache;
import cc.baka9.catseedlogin.bukkit.config.Config;
import cc.baka9.catseedlogin.bukkit.platform.PluginContext;
import cc.baka9.catseedlogin.bukkit.scheduler.CatScheduler;
import cc.baka9.catseedlogin.bukkit.event.CatSeedPlayerLoginEvent;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.common.model.LoginPlayer;
import cc.baka9.catseedlogin.common.util.Crypt;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandLogin extends AbstractCommandSupport {
  @Override
  protected boolean onPlayerCommand(Player player, String[] args) {
    if (args.length == 0) return false;
    String name = player.getName();
    if (LoginPlayerHelper.isLogin(name)) {
      player.sendMessage(Config.Language.LOGIN_REPEAT);
      return true;
    }
    LoginPlayer lp = PlayerCache.getIgnoreCase(name);
    if (lp == null) {
      player.sendMessage(Config.Language.LOGIN_NOREGISTER);
      return true;
    }
    if (!Crypt.match(name, args[0], lp.getPassword().trim())) {
      handleLoginFail(player, player, lp);
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
    player.sendMessage(Config.Language.LOGIN_SUCCESS);
    CatScheduler.updateInventory(player);
    LoginPlayerHelper.recordCurrentIP(player, lp);
    if (Config.Settings.AfterLoginBack && Config.Settings.CanTpSpawnLocation) {
      Config.getOfflineLocation(player)
          .ifPresent(location -> CatScheduler.teleport(player, location));
    }
  }

  private void upgradeToArgon2(LoginPlayer lp, String rawPassword) {
    CatScheduler.runTaskAsync(
        () -> {
          try {
            LoginPlayerHelper.changePasswordAndPersist(lp, rawPassword);
          } catch (Exception e) {
            PluginContext.getLogger()
                .warning("Failed to upgrade password hash to Argon2id for " + lp.getName());
          }
        });
  }

  private void handleLoginFail(CommandSender sender, Player player, LoginPlayer lp) {
    sender.sendMessage(Config.Language.LOGIN_FAIL);
    CatSeedPlayerLoginEvent loginEvent =
        new CatSeedPlayerLoginEvent(player, lp.getEmail(), CatSeedPlayerLoginEvent.Result.FAIL);
    Bukkit.getServer().getPluginManager().callEvent(loginEvent);
    if (Config.EmailVerify.Enable) {
      sender.sendMessage(Config.Language.LOGIN_FAIL_IF_FORGET);
    }
  }
}
