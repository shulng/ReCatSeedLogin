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

public class CommandBindEmail extends AbstractCommandSupport {

  @Override
  protected boolean onPlayerCommand(Player player, String[] args) {
    if (args.length == 0) return false;

    CommandSender sender = player;
    String name = player.getName();

    if (!canBindEmail(sender, name)) return true;

    String subCommand = args[0].toLowerCase();
    if ("set".equals(subCommand)) {
      handleSet(sender, name, args);
    } else if ("verify".equals(subCommand)) {
      handleVerify(sender, name, args);
    }

    return true;
  }

  private boolean canBindEmail(CommandSender sender, String name) {
    LoginPlayer lp = PlayerCache.getIgnoreCase(name);
    if (lp == null) {
      sender.sendMessage(MessageKey.NOT_REGISTERED.get());
      return false;
    }
    if (!LoginPlayerHelper.isLogin(name)) {
      sender.sendMessage(MessageKey.NOT_LOGGED_IN.get());
      return false;
    }
    if (!Config.EmailVerify.enable) {
      sender.sendMessage(MessageKey.RESETPASSWORD_EMAIL_DISABLE.get());
      return false;
    }
    return true;
  }

  private void handleSet(CommandSender sender, String name, String[] args) {
    if (args.length <= 1) return;

    LoginPlayer lp = PlayerCache.getIgnoreCase(name);
    if (lp == null) return;
    if (lp.getEmail() != null && ValidationUtil.isValidEmail(lp.getEmail())) {
      sender.sendMessage(MessageKey.EMAIL_ALREADY_BOUND.get());
      return;
    }

    String mail = args[1];
    if (!ValidationUtil.isValidEmail(mail)) {
      sender.sendMessage(MessageKey.EMAIL_FORMAT_INVALID.get());
      return;
    }

    try {
      Optional<EmailCode> existingCode = EmailCode.getByName(name, EmailCode.Type.Bind);
      if (existingCode.isPresent() && existingCode.get().getEmail().equals(mail)) {
        sender.sendMessage(MessageKey.EMAIL_CODE_ALREADY_SENT.get(mail));
        return;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    EmailCode bindEmail;
    try {
      bindEmail =
          EmailCode.create(name, mail, EmailCode.DEFAULT_CODE_DURATION, EmailCode.Type.Bind);
    } catch (Exception e) {
      sender.sendMessage(MessageKey.INTERNAL_ERROR.get());
      e.printStackTrace();
      return;
    }
    sender.sendMessage(MessageKey.SENDING_EMAIL_CODE.get());
    sendEmailCode(sender, name, mail, bindEmail);
  }

  private void handleVerify(CommandSender sender, String name, String[] args) {
    if (args.length <= 1) return;

    LoginPlayer lp = PlayerCache.getIgnoreCase(name);
    if (lp.getEmail() != null && ValidationUtil.isValidEmail(lp.getEmail())) {
      sender.sendMessage(MessageKey.EMAIL_ALREADY_BOUND.get());
      return;
    }

    Optional<EmailCode> emailOptional = EmailCode.getByName(name, EmailCode.Type.Bind);
    if (!emailOptional.isPresent()) {
      sender.sendMessage(MessageKey.NO_PENDING_EMAIL_CODE.get());
      return;
    }

    EmailCode bindEmail = emailOptional.get();
    if (!bindEmail.getCode().equals(args[1])) {
      sender.sendMessage(MessageKey.VERIFICATION_CODE_INCORRECT.get());
      return;
    }

    sender.sendMessage(MessageKey.BINDING_EMAIL.get());
    bindEmail(sender, lp, bindEmail);
  }

  private void sendEmailCode(CommandSender sender, String name, String mail, EmailCode bindEmail) {
    String content = buildBindEmailContent(name, bindEmail);
    EmailSender.sendEmailAsync(
        mail,
        MessageKey.EMAIL_SUBJECT_BIND_EMAIL.get(),
        content,
        () -> notifyBindEmailSent(sender, mail),
        () -> notifyBindEmailFailed(sender));
  }

  private String buildBindEmailContent(String name, EmailCode bindEmail) {
    long minutes = bindEmail.getDurability() / (1000 * 60);
    return MessageKey.EMAIL_BIND_EMAIL_CONTENT.get(bindEmail.getCode(), name, minutes);
  }

  private void notifyBindEmailSent(CommandSender sender, String mail) {
    sender.sendMessage(MessageKey.EMAIL_SENT_CHECK_INBOX.get(mail));
    sender.sendMessage(MessageKey.CHECK_SPAM_FOLDER.get());
  }

  private void notifyBindEmailFailed(CommandSender sender) {
    sender.sendMessage(MessageKey.EMAIL_SEND_FAILED.get());
  }

  private void bindEmail(CommandSender sender, LoginPlayer lp, EmailCode bindEmail) {
    CatScheduler.runTaskAsync(() -> executeBindEmail(sender, lp, bindEmail));
  }

  private void executeBindEmail(CommandSender sender, LoginPlayer lp, EmailCode bindEmail) {
    try {
      lp.setEmail(bindEmail.getEmail());
      BukkitContext.getSql().edit(lp);
      PlayerCache.refresh(lp.getName());
      notifyBindSuccess(sender, bindEmail);
    } catch (Exception e) {
      e.printStackTrace();
      sender.sendMessage(MessageKey.INTERNAL_ERROR.get());
    }
  }

  private void notifyBindSuccess(CommandSender sender, EmailCode bindEmail) {
    Player syncPlayer = Bukkit.getPlayer(((Player) sender).getUniqueId());
    if (syncPlayer == null || !syncPlayer.isOnline()) return;

    syncPlayer.sendMessage(MessageKey.EMAIL_BOUND_SUCCESS.get(bindEmail.getEmail()));
    EmailCode.removeByName(syncPlayer.getName(), EmailCode.Type.Bind);
  }
}
