package cc.baka9.catseedlogin.bukkit.command;

import cc.baka9.catseedlogin.bukkit.BukkitContext;
import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.event.CatSeedPlayerRegisterEvent;
import cc.baka9.catseedlogin.bukkit.scheduler.CatScheduler;
import cc.baka9.catseedlogin.bukkit.session.LoginPlayerHelper;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import cc.baka9.catseedlogin.common.model.LoginPlayer;
import cc.baka9.catseedlogin.common.util.PasswordHelper;
import cc.baka9.catseedlogin.common.util.ValidationUtil;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** {@code /register} 玩家注册指令。 */
public class CommandRegister implements CommandExecutor {

  private static BukkitConfigManager config() {
    return BukkitContext.getConfigManager();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length != 2 || !(sender instanceof Player)) return false;
    Player player = (Player) sender;
    String name = sender.getName();

    if (!canRegister(player, name)) {
      return true;
    }
    if (!args[0].equals(args[1])) {
      sender.sendMessage(MessageKey.REGISTER_PASSWORD_CONFIRM_FAIL.get());
      return true;
    }
    if (ValidationUtil.isPasswordTooSimple(args[0])) {
      sender.sendMessage(MessageKey.COMMON_PASSWORD_SO_SIMPLE.get());
      return true;
    }
    if (!Cache.isLoaded) return true;

    sender.sendMessage(MessageKey.REGISTERING.get());
    registerPlayerAsync(player, name, args[0]);
    return true;
  }

  private boolean canRegister(Player player, String name) {
    if (config().isBedrockLoginBypass() && LoginPlayerHelper.isFloodgatePlayer(player)) {
      return false;
    }
    if (LoginPlayerHelper.isLogin(name)) {
      player.sendMessage(MessageKey.REGISTER_AFTER_LOGIN_ALREADY.get());
      return false;
    }
    if (LoginPlayerHelper.isRegister(name)) {
      player.sendMessage(MessageKey.REGISTER_BEFORE_LOGIN_ALREADY.get());
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
    CatScheduler.runTaskAsync(
        () -> {
          try {
            processRegistration(player, name, password, currentIp, isLoopback);
          } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(MessageKey.INTERNAL_ERROR.get());
          }
        });
  }

  private void processRegistration(
      Player player, String name, String password, String currentIp, boolean isLoopback)
      throws Exception {
    List<LoginPlayer> loginPlayersByIp = BukkitContext.getSql().getLikeByIp(currentIp);

    if (!isLoopback && loginPlayersByIp.size() >= config().getIpRegisterCountLimit()) {
      player.sendMessage(
          MessageKey.REGISTER_MORE
              .get()
              .replace("{count}", String.valueOf(loginPlayersByIp.size()))
              .replace(
                  "{accounts}",
                  String.join(
                      ", ",
                      loginPlayersByIp.stream().map(LoginPlayer::getName).toArray(String[]::new))));
      return;
    }

    LoginPlayer lp = PasswordHelper.registerNewPlayer(name, password);
    BukkitContext.getSql().add(lp);
    Cache.refresh(lp.getName());
    LoginPlayerHelper.add(lp);
    CatScheduler.runTask(
        () -> {
          CatSeedPlayerRegisterEvent event = new CatSeedPlayerRegisterEvent(Bukkit.getPlayer(name));
          Bukkit.getServer().getPluginManager().callEvent(event);
        });
    player.sendMessage(MessageKey.REGISTER_SUCCESS.get());
    CatScheduler.updateInventory(player);
    LoginPlayerHelper.recordCurrentIP(player, lp);
  }
}
