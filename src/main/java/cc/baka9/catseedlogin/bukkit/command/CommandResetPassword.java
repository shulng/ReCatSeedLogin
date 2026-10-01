package cc.baka9.catseedlogin.bukkit.command;

import cc.baka9.catseedlogin.bukkit.BukkitContext;
import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.scheduler.CatScheduler;
import cc.baka9.catseedlogin.bukkit.session.LoginPlayerHelper;
import cc.baka9.catseedlogin.common.email.EmailSender;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import cc.baka9.catseedlogin.common.model.EmailCode;
import cc.baka9.catseedlogin.common.model.LoginPlayer;
import cc.baka9.catseedlogin.common.util.PasswordHelper;
import cc.baka9.catseedlogin.common.util.ValidationUtil;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** {@code /resetpassword} 通过已绑定邮箱重置密码。 */
public class CommandResetPassword implements CommandExecutor {

  private static final long EMAIL_CODE_DURATION = 1000 * 60 * 5;

  private static BukkitConfigManager config() {
    return BukkitContext.getConfigManager();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 0 || !(sender instanceof Player)) return false;

    Player player = (Player) sender;
    String name = player.getName();

    if (config().isBedrockLoginBypass() && LoginPlayerHelper.isFloodgatePlayer(player)) return true;

    LoginPlayer lp = Cache.getIgnoreCase(name);
    if (lp == null) {
      sender.sendMessage(MessageKey.RESETPASSWORD_NOREGISTER.get());
      return true;
    }
    if (!config().isEmailEnable()) {
      sender.sendMessage(MessageKey.RESETPASSWORD_EMAIL_DISABLE.get());
      return true;
    }

    if (args[0].equalsIgnoreCase("forget")) {
      return handleForget(sender, name, lp);
    }

    if (args[0].equalsIgnoreCase("re") && args.length > 2) {
      return handleReset(player, lp, args[1], args[2]);
    }

    return true;
  }

  private boolean handleForget(CommandSender sender, String name, LoginPlayer lp) {
    if (lp.getEmail() == null) {
      sender.sendMessage(MessageKey.RESETPASSWORD_EMAIL_NO_SET.get());
      return true;
    }

    try {
      Optional<EmailCode> optional = EmailCode.getByName(name, EmailCode.Type.RESET_PASSWORD);
      if (optional.isPresent()) {
        sender.sendMessage(
            MessageKey.RESETPASSWORD_EMAIL_REPEAT_SEND_MESSAGE
                .get()
                .replace("{email}", optional.get().getEmail()));
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    EmailCode emailCode;
    try {
      emailCode =
          EmailCode.create(name, lp.getEmail(), EMAIL_CODE_DURATION, EmailCode.Type.RESET_PASSWORD);
    } catch (Exception e) {
      sender.sendMessage(MessageKey.INTERNAL_ERROR.get());
      e.printStackTrace();
      return true;
    }
    sender.sendMessage(
        MessageKey.RESETPASSWORD_EMAIL_SENDING_MESSAGE.get().replace("{email}", lp.getEmail()));

    sendResetEmailAsync(sender, name, emailCode);
    return true;
  }

  private void sendResetEmailAsync(CommandSender sender, String name, EmailCode emailCode) {
    CatScheduler.runTaskAsync(
        () -> {
          try {
            String content = buildResetEmailContent(name, emailCode);
            EmailSender.send(
                config(),
                emailCode.getEmail(),
                MessageKey.EMAIL_SUBJECT_RESET_PASSWORD.get(),
                content);
            notifyEmailSent(sender, emailCode.getEmail());
          } catch (Exception e) {
            notifyEmailFailed(sender);
            e.printStackTrace();
          }
        });
  }

  private String buildResetEmailContent(String name, EmailCode emailCode) {
    long minutes = emailCode.getDurability() / (1000 * 60);
    return MessageKey.EMAIL_RESET_PASSWORD_CONTENT.get(emailCode.getCode(), name, minutes);
  }

  private void notifyEmailSent(CommandSender sender, String email) {
    CatScheduler.runTask(
        () ->
            sender.sendMessage(
                MessageKey.RESETPASSWORD_EMAIL_SENT_MESSAGE.get().replace("{email}", email)));
  }

  private void notifyEmailFailed(CommandSender sender) {
    CatScheduler.runTask(() -> sender.sendMessage(MessageKey.RESETPASSWORD_EMAIL_WARN.get()));
  }

  private boolean handleReset(Player player, LoginPlayer lp, String code, String pwd) {
    if (lp.getEmail() == null) {
      player.sendMessage(MessageKey.RESETPASSWORD_EMAIL_NO_SET.get());
      return true;
    }

    try {
      Optional<EmailCode> optional =
          EmailCode.getByName(lp.getName(), EmailCode.Type.RESET_PASSWORD);
      if (!optional.isPresent()) {
        player.sendMessage(MessageKey.RESETPASSWORD_FAIL.get());
        return true;
      }
      if (!optional.get().getCode().equals(code)) {
        player.sendMessage(MessageKey.RESETPASSWORD_EMAILCODE_INCORRECT.get());
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
      player.sendMessage(MessageKey.RESETPASSWORD_FAIL.get());
      return true;
    }

    if (ValidationUtil.isPasswordTooSimple(pwd)) {
      player.sendMessage(MessageKey.COMMON_PASSWORD_SO_SIMPLE.get());
      return true;
    }

    player.sendMessage(MessageKey.RESETTING_PASSWORD.get());
    String name = lp.getName();
    CatScheduler.runTaskAsync(() -> executePasswordReset(name, lp, pwd));
    return true;
  }

  private void executePasswordReset(String name, LoginPlayer lp, String pwd) {
    try {
      LoginPlayer copy = PasswordHelper.updatePassword(lp, pwd);
      BukkitContext.getSql().edit(copy);
      Cache.refresh(name);
      LoginPlayerHelper.remove(lp);
      EmailCode.removeByName(name, EmailCode.Type.RESET_PASSWORD);
      notifyResetSuccess(name);
    } catch (Exception e) {
      BukkitContext.getLogger().warning("重置玩家密码失败: " + name);
      e.printStackTrace();
    }
  }

  private void notifyResetSuccess(String name) {
    Player p = Bukkit.getPlayer(name);
    if (p == null || !p.isOnline()) return;

    if (config().isCanTpSpawnLocation()) {
      CatScheduler.teleport(p, config().getBukkitSpawnLocation());
    }
    p.sendMessage(MessageKey.RESETPASSWORD_SUCCESS.get());

    if (BukkitContext.isLoadProtocolLib()) {
      LoginPlayerHelper.sendBlankInventoryPacket(p);
    }
  }
}
