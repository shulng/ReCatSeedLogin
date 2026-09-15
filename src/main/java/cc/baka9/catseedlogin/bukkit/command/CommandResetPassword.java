package cc.baka9.catseedlogin.bukkit.command;

import cc.baka9.catseedlogin.bukkit.cache.PlayerCache;
import cc.baka9.catseedlogin.bukkit.config.Config;
import cc.baka9.catseedlogin.bukkit.object.EmailCode;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.bukkit.platform.BukkitContext;
import cc.baka9.catseedlogin.bukkit.scheduler.CatScheduler;
import cc.baka9.catseedlogin.bukkit.util.EmailSender;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import cc.baka9.catseedlogin.common.model.LoginPlayer;
import cc.baka9.catseedlogin.common.util.ValidationUtil;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandResetPassword extends AbstractCommandSupport {

  @Override
  protected boolean onPlayerCommand(Player player, String[] args) {
    if (args.length == 0) return false;

    CommandSender sender = player;
    String name = player.getName();

    LoginPlayer lp = PlayerCache.getIgnoreCase(name);
    if (lp == null) {
      sender.sendMessage(Config.Language.resetPasswordNoRegister);
      return true;
    }
    if (!Config.EmailVerify.enable) {
      sender.sendMessage(Config.Language.resetPasswordEmailDisable);
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
      sender.sendMessage(Config.Language.resetPasswordEmailNoSet);
      return true;
    }

    try {
      Optional<EmailCode> optional = EmailCode.getByName(name, EmailCode.Type.ResetPassword);
      if (optional.isPresent()) {
        sender.sendMessage(
            Config.Language.resetPasswordEmailRepeatSendMessage.replace(
                "{email}", optional.get().getEmail()));
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    EmailCode emailCode;
    try {
      emailCode =
          EmailCode.create(
              name, lp.getEmail(), EmailCode.DEFAULT_CODE_DURATION, EmailCode.Type.ResetPassword);
    } catch (Exception e) {
      sender.sendMessage(MessageKey.INTERNAL_ERROR.get());
      e.printStackTrace();
      return true;
    }
    sender.sendMessage(
        Config.Language.resetPasswordEmailSendingMessage.replace("{email}", lp.getEmail()));

    String content = buildResetEmailContent(name, emailCode);
    EmailSender.sendEmailAsync(
        emailCode.getEmail(),
        MessageKey.EMAIL_SUBJECT_RESET_PASSWORD.get(),
        content,
        () -> notifyEmailSent(sender, emailCode.getEmail()),
        () -> notifyEmailFailed(sender));
    return true;
  }

  private String buildResetEmailContent(String name, EmailCode emailCode) {
    long minutes = emailCode.getDurability() / (1000 * 60);
    return MessageKey.EMAIL_RESET_PASSWORD_CONTENT.get(emailCode.getCode(), name, minutes);
  }

  private void notifyEmailSent(CommandSender sender, String email) {
    sender.sendMessage(
        Config.Language.resetPasswordEmailSentMessage.replace("{email}", email));
  }

  private void notifyEmailFailed(CommandSender sender) {
    sender.sendMessage(Config.Language.resetPasswordEmailWarn);
  }

  private boolean handleReset(Player player, LoginPlayer lp, String code, String pwd) {
    CommandSender sender = player;
    if (lp.getEmail() == null) {
      sender.sendMessage(Config.Language.resetPasswordEmailNoSet);
      return true;
    }

    try {
      Optional<EmailCode> optional =
          EmailCode.getByName(lp.getName(), EmailCode.Type.ResetPassword);
      if (!optional.isPresent()) {
        sender.sendMessage(Config.Language.resetPasswordFail);
        return true;
      }
      if (!optional.get().getCode().equals(code)) {
        sender.sendMessage(Config.Language.resetPasswordEmailCodeIncorrect);
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
      sender.sendMessage(Config.Language.resetPasswordFail);
      return true;
    }

    if (ValidationUtil.isPasswordTooSimple(pwd)) {
      sender.sendMessage(Config.Language.commonPasswordSoSimple);
      return true;
    }

    sender.sendMessage(MessageKey.RESETTING_PASSWORD.get());
    processPasswordResetAsync(player, lp, pwd);
    return true;
  }

  private void processPasswordResetAsync(Player player, LoginPlayer lp, String pwd) {
    CommandSender sender = player;
    String name = lp.getName();
    CatScheduler.runTaskAsync(
        () -> {
          executePasswordReset(name, lp, pwd, sender);
        });
  }

  private void executePasswordReset(String name, LoginPlayer lp, String pwd, CommandSender sender) {
    try {
      LoginPlayerHelper.changePasswordAndPersist(lp, pwd);
      LoginPlayerHelper.remove(lp);
      EmailCode.removeByName(name, EmailCode.Type.ResetPassword);
      Player player = Bukkit.getPlayer(name);
      notifyResetSuccess(name, player);
    } catch (Exception e) {
      sender.sendMessage(MessageKey.DATABASE_ERROR.get());
      e.printStackTrace();
    }
  }

  private void notifyResetSuccess(String name, Player player) {
    Player p = Bukkit.getPlayer(name);
    if (p == null || !p.isOnline()) return;

    if (Config.Settings.canTpSpawnLocation) {
      CatScheduler.teleport(p, Config.Settings.spawnLocation);
    }
    p.sendMessage(Config.Language.resetPasswordSuccess);

    if (BukkitContext.isLoadProtocolLib()) {
      LoginPlayerHelper.sendBlankInventoryPacket(p);
    }
  }
}
