package cc.baka9.catseedlogin.bukkit.command;

import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import cc.baka9.catseedlogin.bukkit.CatScheduler;
import cc.baka9.catseedlogin.bukkit.Config;
import cc.baka9.catseedlogin.bukkit.PluginContext;
import cc.baka9.catseedlogin.bukkit.Cache;
import cc.baka9.catseedlogin.bukkit.object.EmailCode;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import cc.baka9.catseedlogin.common.model.LoginPlayer;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.bukkit.util.EmailSender;
import cc.baka9.catseedlogin.common.util.ValidationUtil;

public class CommandBindEmail implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 0 || !(sender instanceof Player)) return false;

        String name = sender.getName();

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
        Player player = (Player) sender;

        if (Config.Settings.BedrockLoginBypass && LoginPlayerHelper.isFloodgatePlayer(player)) return false;

        LoginPlayer lp = Cache.getIgnoreCase(name);
        if (lp == null) {
            sender.sendMessage(MessageKey.NOT_REGISTERED.get());
            return false;
        }
        if (!LoginPlayerHelper.isLogin(name)) {
            sender.sendMessage(MessageKey.NOT_LOGGED_IN.get());
            return false;
        }
        if (!Config.EmailVerify.Enable) {
            sender.sendMessage(MessageKey.RESETPASSWORD_EMAIL_DISABLE.get());
            return false;
        }
        return true;
    }

    private void handleSet(CommandSender sender, String name, String[] args) {
        if (args.length <= 1) return;

        LoginPlayer lp = Cache.getIgnoreCase(name);
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
            bindEmail = EmailCode.create(name, mail, 1000 * 60 * 5, EmailCode.Type.Bind);
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

        LoginPlayer lp = Cache.getIgnoreCase(name);
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
        CatScheduler.runTaskAsync(() -> {
            try {
                String content = buildBindEmailContent(name, bindEmail);
                EmailSender.sendEmail(mail, MessageKey.EMAIL_SUBJECT_BIND_EMAIL.get(), content);
                notifyBindEmailSent(sender, mail);
            } catch (Exception e) {
                notifyBindEmailFailed(sender);
                e.printStackTrace();
            }
        });
    }

    private String buildBindEmailContent(String name, EmailCode bindEmail) {
        long minutes = bindEmail.getDurability() / (1000 * 60);
        return MessageKey.EMAIL_BIND_EMAIL_CONTENT.get(bindEmail.getCode(), name, minutes);
    }

    private void notifyBindEmailSent(CommandSender sender, String mail) {
        CatScheduler.runTask(() -> {
            sender.sendMessage(MessageKey.EMAIL_SENT_CHECK_INBOX.get(mail));
            sender.sendMessage(MessageKey.CHECK_SPAM_FOLDER.get());
        });
    }

    private void notifyBindEmailFailed(CommandSender sender) {
        CatScheduler.runTask(() -> sender.sendMessage(MessageKey.EMAIL_SEND_FAILED.get()));
    }

    private void bindEmail(CommandSender sender, LoginPlayer lp, EmailCode bindEmail) {
        CatScheduler.runTaskAsync(() -> executeBindEmail(sender, lp, bindEmail));
    }

    private void executeBindEmail(CommandSender sender, LoginPlayer lp, EmailCode bindEmail) {
        try {
            lp.setEmail(bindEmail.getEmail());
            PluginContext.getSql().edit(lp);
            Cache.refresh(lp.getName());
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
