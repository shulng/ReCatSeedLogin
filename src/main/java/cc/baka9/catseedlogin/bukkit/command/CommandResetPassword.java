package cc.baka9.catseedlogin.bukkit.command;

import java.util.Optional;
import java.security.MessageDigest;
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

public class CommandResetPassword implements CommandExecutor {
    private static final long EMAIL_CODE_DURATION = 1000 * 60 * 5;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 0 || !(sender instanceof Player)) return false;

        Player player = (Player) sender;
        String name = player.getName();

        if (Config.Settings.BedrockLoginBypass && LoginPlayerHelper.isFloodgatePlayer(player)) return true;

        LoginPlayer lp = Cache.getIgnoreCase(name);
        if (lp == null) {
            sender.sendMessage(Config.Language.RESETPASSWORD_NOREGISTER);
            return true;
        }
        if (!Config.EmailVerify.Enable) {
            sender.sendMessage(Config.Language.RESETPASSWORD_EMAIL_DISABLE);
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
            sender.sendMessage(Config.Language.RESETPASSWORD_EMAIL_NO_SET);
            return true;
        }

        try {
            Optional<EmailCode> optional = EmailCode.getByName(name, EmailCode.Type.ResetPassword);
            if (optional.isPresent()) {
                sender.sendMessage(Config.Language.RESETPASSWORD_EMAIL_REPEAT_SEND_MESSAGE
                        .replace("{email}", optional.get().getEmail()));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        EmailCode emailCode;
        try {
            emailCode = EmailCode.create(name, lp.getEmail(), EMAIL_CODE_DURATION, EmailCode.Type.ResetPassword);
        } catch (Exception e) {
            sender.sendMessage(MessageKey.INTERNAL_ERROR.get());
            e.printStackTrace();
            return true;
        }
        sender.sendMessage(Config.Language.RESETPASSWORD_EMAIL_SENDING_MESSAGE
                .replace("{email}", lp.getEmail()));

        sendResetEmailAsync(sender, name, emailCode);
        return true;
    }

    private void sendResetEmailAsync(CommandSender sender, String name, EmailCode emailCode) {
        CatScheduler.runTaskAsync(() -> {
            try {
                String content = buildResetEmailContent(name, emailCode);
                EmailSender.sendEmail(emailCode.getEmail(), MessageKey.EMAIL_SUBJECT_RESET_PASSWORD.get(), content);
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
        Bukkit.getScheduler().runTask(PluginContext.getPlugin(), () ->
                sender.sendMessage(Config.Language.RESETPASSWORD_EMAIL_SENT_MESSAGE.replace("{email}", email)));
    }

    private void notifyEmailFailed(CommandSender sender) {
        Bukkit.getScheduler().runTask(PluginContext.getPlugin(), () ->
                sender.sendMessage(Config.Language.RESETPASSWORD_EMAIL_WARN));
    }

    private boolean handleReset(Player player, LoginPlayer lp, String code, String pwd) {
        CommandSender sender = player;
        if (lp.getEmail() == null) {
            sender.sendMessage(Config.Language.RESETPASSWORD_EMAIL_NO_SET);
            return true;
        }

        try {
            Optional<EmailCode> optional = EmailCode.getByName(lp.getName(), EmailCode.Type.ResetPassword);
            if (!optional.isPresent()) {
                sender.sendMessage(Config.Language.RESETPASSWORD_FAIL);
                return true;
            }
            if (!MessageDigest.isEqual(optional.get().getCode().getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    code.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
                sender.sendMessage(Config.Language.RESETPASSWORD_EMAILCODE_INCORRECT);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(Config.Language.RESETPASSWORD_FAIL);
            return true;
        }

        if (ValidationUtil.isPasswordTooSimple(pwd)) {
            sender.sendMessage(Config.Language.COMMON_PASSWORD_SO_SIMPLE);
            return true;
        }

        sender.sendMessage(MessageKey.RESETTING_PASSWORD.get());
        processPasswordResetAsync(player, lp, pwd);
        return true;
    }

    private void processPasswordResetAsync(Player player, LoginPlayer lp, String pwd) {
        CommandSender sender = player;
        String name = lp.getName();
        CatScheduler.runTaskAsync(() -> {
            executePasswordReset(name, lp, pwd, sender);
        });
    }

    private void executePasswordReset(String name, LoginPlayer lp, String pwd, CommandSender sender) {
        try {
            LoginPlayer copy = lp.copy();
            copy.setPassword(pwd);
            copy.crypt();
            PluginContext.getSql().edit(copy);
            Cache.refresh(name);
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

        if (Config.Settings.CanTpSpawnLocation) {
            CatScheduler.teleport(p, Config.Settings.SpawnLocation);
        }
        p.sendMessage(Config.Language.RESETPASSWORD_SUCCESS);

        if (PluginContext.isLoadProtocolLib()) {
            LoginPlayerHelper.sendBlankInventoryPacket(p);
        }
    }
}