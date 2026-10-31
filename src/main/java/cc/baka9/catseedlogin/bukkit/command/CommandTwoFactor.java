package cc.baka9.catseedlogin.bukkit.command;

import cc.baka9.catseedlogin.bukkit.Cache;
import cc.baka9.catseedlogin.bukkit.CatScheduler;
import cc.baka9.catseedlogin.bukkit.Config;
import cc.baka9.catseedlogin.bukkit.PluginContext;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayer;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.bukkit.object.TotpRecoveryCode;
import cc.baka9.catseedlogin.bukkit.object.TwoFactorLimiter;
import cc.baka9.catseedlogin.bukkit.object.TwoFactorState;
import cc.baka9.catseedlogin.bukkit.util.RecoveryCodeGenerator;
import cc.baka9.catseedlogin.bukkit.util.TwoFactorMessageUtil;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import cc.baka9.catseedlogin.common.util.SecretCodec;
import cc.baka9.catseedlogin.common.util.TabCompleteUtil;
import cc.baka9.catseedlogin.common.util.TotpUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * 两步验证指令。
 *
 * <p>本阶段覆盖绑定与关闭：{@code /2fa enable} 生成密钥、{@code /2fa verify} 确认绑定、
 * {@code /2fa disable} 关闭。恢复码只用于第二步验证，不能用于登录第一步或重置密码。
 */
public class CommandTwoFactor implements CommandExecutor, TabCompleter {

  private static final List<String> SUB_COMMANDS =
      Arrays.asList("enable", "verify", "recover", "disable");

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(MessageKey.TWO_FACTOR_PLAYER_ONLY.get());
      return true;
    }
    Player player = (Player) sender;
    if (args.length == 0) {
      player.sendMessage(MessageKey.TWO_FACTOR_USAGE.get());
      return true;
    }
    String sub = args[0].toLowerCase(Locale.ROOT);
    if ("enable".equals(sub)) {
      return enable(player);
    }
    if ("verify".equals(sub)) {
      return verify(player, args);
    }
    if ("recover".equals(sub)) {
      return recover(player, args);
    }
    if ("disable".equals(sub)) {
      return disable(player, args);
    }
    player.sendMessage(MessageKey.TWO_FACTOR_USAGE.get());
    return true;
  }

  /** 生成密钥并交给玩家，确认之前不会写库。 */
  private boolean enable(Player player) {
    if (!Config.Settings.TwoFactorEnabled) {
      player.sendMessage(MessageKey.TWO_FACTOR_DISABLED_GLOBAL.get());
      return true;
    }
    String name = player.getName();
    if (!LoginPlayerHelper.isLogin(name)) {
      player.sendMessage(MessageKey.TWO_FACTOR_NEED_LOGIN.get());
      return true;
    }
    LoginPlayer lp = Cache.getIgnoreCase(name);
    if (lp == null) {
      player.sendMessage(MessageKey.TWO_FACTOR_NEED_LOGIN.get());
      return true;
    }
    if (lp.isTotpEnabled()) {
      player.sendMessage(MessageKey.TWO_FACTOR_ALREADY_ENABLED.get());
      return true;
    }
    String secret = TotpUtil.generateSecret();
    TwoFactorState.putPendingBind(name, secret);
    sendSecret(player, secret);
    return true;
  }

  /**
   * 校验验证码。
   *
   * <p>有两种待验证状态：绑定确认（刚执行过 {@code /2fa enable}）和登录待验证（密码已通过）。
   * 前者确认后写入密钥并生成恢复码，后者确认后才真正放行登录。
   */
  private boolean verify(Player player, String[] args) {
    if (args.length < 2) {
      player.sendMessage(MessageKey.TWO_FACTOR_USAGE.get());
      return true;
    }
    String name = player.getName();
    String secret = TwoFactorState.getPendingBind(name);
    if (secret != null) {
      return verifyBind(player, name, secret, args[1]);
    }
    TwoFactorState.PendingLogin pending = TwoFactorState.getPendingLogin(name);
    if (pending != null) {
      return verifyLogin(player, pending, args[1]);
    }
    player.sendMessage(MessageKey.TWO_FACTOR_NO_PENDING.get());
    return true;
  }

  /** 确认绑定：校验当前 TOTP，通过才落库并生成恢复码。 */
  private boolean verifyBind(Player player, String name, String secret, String code) {
    if (!TotpUtil.verify(secret, code, System.currentTimeMillis(), digits(), period(), 1)) {
      handleVerifyFailure(player);
      return true;
    }
    TwoFactorLimiter.recordSuccess(player.getUniqueId());
    TwoFactorState.removePendingBind(name);
    enableTwoFactor(player, name, secret);
    return true;
  }

  /** 完成登录：校验当前 TOTP，通过后才真正放行，之前的登录前限制此时才解除。 */
  private boolean verifyLogin(Player player, TwoFactorState.PendingLogin pending, String code) {
    LoginPlayer lp = pending.getLoginPlayer();
    String secret = SecretCodec.decrypt(lp.getTotpSecret());
    if (secret == null) {
      TwoFactorState.removePendingLogin(lp.getName());
      player.sendMessage(MessageKey.TWO_FACTOR_SECRET_BROKEN.get());
      return true;
    }
    if (!TotpUtil.verify(secret, code, System.currentTimeMillis(), digits(), period(), 1)) {
      handleVerifyFailure(player);
      return true;
    }
    TwoFactorLimiter.recordSuccess(player.getUniqueId());
    TwoFactorState.removePendingLogin(lp.getName());
    LoginPlayerHelper.completeLogin(player, lp);
    return true;
  }

  /**
   * 用一次性恢复码代替 TOTP 完成第二步验证。
   *
   * <p>恢复码只做这一件事：不能用于登录的第一步，不能用于重置密码，也不能用于关闭两步验证。
   *
   * <p>成功后该恢复码标记为已用，两步验证关闭并进入重绑流程——玩家此时是正常登录状态，
   * 可以正常游戏，之后随时执行 {@code /2fa enable} 重新绑定验证器。
   */
  private boolean recover(Player player, String[] args) {
    if (args.length < 2) {
      player.sendMessage(MessageKey.TWO_FACTOR_USAGE.get());
      return true;
    }
    String name = player.getName();
    TwoFactorState.PendingLogin pending = TwoFactorState.getPendingLogin(name);
    if (pending == null) {
      player.sendMessage(MessageKey.TWO_FACTOR_RECOVER_NOT_PENDING.get());
      return true;
    }
    // 恢复码是 Argon2 哈希，逐条比对开销不小，放到异步线程
    CatScheduler.runTaskAsync(
        () -> {
          TotpRecoveryCode matched = PluginContext.getSql().findUnusedRecoveryCode(name, args[1]);
          CatScheduler.runTask(() -> {
            if (matched == null) {
              handleVerifyFailure(player);
              return;
            }
            finishRecover(player, pending, matched);
          });
        });
    return true;
  }

  /** 恢复码验证通过：作废恢复码、关闭两步验证、进入重绑流程，然后放行登录。 */
  private void finishRecover(
      Player player, TwoFactorState.PendingLogin pending, TotpRecoveryCode matched) {
    String name = pending.getLoginPlayer().getName();
    PluginContext.getSql().markRecoveryCodeUsed(name, matched.getCodeHash());
    PluginContext.getSql().invalidateRecoveryCodes(name);
    PluginContext.getSql().updateTotpSecret(name, null);
    PluginContext.getSql().updateTotpEnabled(name, false);
    Cache.refresh(name);

    TwoFactorLimiter.recordSuccess(player.getUniqueId());
    TwoFactorState.removePendingLogin(name);
    TwoFactorState.setRebindRequired(name);

    LoginPlayerHelper.completeLogin(player, pending.getLoginPlayer());
    player.sendMessage(MessageKey.TWO_FACTOR_RECOVER_SUCCESS.get());
  }

  /** 校验失败：计入限流，超过上限就踢出。 */
  private void handleVerifyFailure(Player player) {
    boolean exceeded =
        TwoFactorLimiter.recordFailure(
            player.getUniqueId(),
            Config.Settings.TwoFactorMaxAttempts,
            Config.Settings.TwoFactorAttemptWindow * 1000L);
    if (!exceeded) {
      player.sendMessage(MessageKey.TWO_FACTOR_INVALID_CODE.get());
      return;
    }
    player.sendMessage(MessageKey.TWO_FACTOR_MAX_ATTEMPTS.get());
    player.kickPlayer(MessageKey.TWO_FACTOR_MAX_ATTEMPTS.get());
  }

  /** 关闭两步验证，只接受 TOTP，成功后作废该玩家所有恢复码。 */
  private boolean disable(Player player, String[] args) {
    if (args.length < 2) {
      player.sendMessage(MessageKey.TWO_FACTOR_USAGE.get());
      return true;
    }
    String name = player.getName();
    if (!LoginPlayerHelper.isLogin(name)) {
      player.sendMessage(MessageKey.TWO_FACTOR_NEED_LOGIN.get());
      return true;
    }
    LoginPlayer lp = Cache.getIgnoreCase(name);
    if (lp == null || !lp.isTotpEnabled()) {
      player.sendMessage(MessageKey.TWO_FACTOR_NOT_ENABLED.get());
      return true;
    }
    String secret = SecretCodec.decrypt(lp.getTotpSecret());
    if (secret == null) {
      player.sendMessage(MessageKey.TWO_FACTOR_SECRET_BROKEN.get());
      return true;
    }
    if (!TotpUtil.verify(secret, args[1], System.currentTimeMillis(), digits(), period(), 1)) {
      handleVerifyFailure(player);
      return true;
    }
    TwoFactorLimiter.recordSuccess(player.getUniqueId());
    PluginContext.getSql().updateTotpSecret(name, null);
    PluginContext.getSql().updateTotpEnabled(name, false);
    PluginContext.getSql().invalidateRecoveryCodes(name);
    Cache.refresh(name);
    player.sendMessage(MessageKey.TWO_FACTOR_DISABLED.get());
    return true;
  }

  private void enableTwoFactor(Player player, String name, String secret) {
    String encrypted;
    try {
      encrypted = SecretCodec.encrypt(secret);
    } catch (Exception e) {
      player.sendMessage(MessageKey.TWO_FACTOR_SECRET_BROKEN.get());
      return;
    }
    PluginContext.getSql().updateTotpSecret(name, encrypted);
    PluginContext.getSql().updateTotpEnabled(name, true);
    Cache.refresh(name);
    TwoFactorState.removeRebindRequired(name);
    player.sendMessage(MessageKey.TWO_FACTOR_ENABLED.get());

    // 恢复码的哈希是 Argon2，耗时较长，放到异步线程里生成，再切回主线程展示
    CatScheduler.runTaskAsync(
        () -> {
          RecoveryCodeGenerator.Generated generated =
              RecoveryCodeGenerator.generate(name, Config.Settings.TwoFactorRecoveryCodeCount);
          PluginContext.getSql().saveRecoveryCodes(name, generated.getEntries());
          CatScheduler.runTask(() -> showRecoveryCodes(player, generated.getRawCodes()));
        });
  }

  private void sendSecret(Player player, String secret) {
    String uri =
        TotpUtil.buildOtpAuthUri(Config.Settings.TwoFactorIssuer, player.getName(), secret);
    TwoFactorMessageUtil.sendText(player, MessageKey.TWO_FACTOR_SECRET_SENT.get());
    TwoFactorMessageUtil.send(
        player,
        Collections.singletonList(
            new TwoFactorMessageUtil.MessageLine(
                MessageKey.TWO_FACTOR_SECRET_LABEL.get(secret),
                secret,
                MessageKey.TWO_FACTOR_SECRET_HOVER.get())));
    TwoFactorMessageUtil.send(
        player,
        Collections.singletonList(
            new TwoFactorMessageUtil.MessageLine(
                MessageKey.TWO_FACTOR_URI_LABEL.get(uri),
                uri,
                MessageKey.TWO_FACTOR_URI_HOVER.get())));
    TwoFactorMessageUtil.sendText(player, MessageKey.TWO_FACTOR_VERIFY_PROMPT.get());
  }

  /** 展示恢复码：先红色警告，再给「复制全部」和逐条可点击的复制行，分批延迟发送避免刷屏。 */
  private void showRecoveryCodes(Player player, List<String> codes) {
    player.sendMessage(MessageKey.TWO_FACTOR_RECOVERY_WARN.get());
    if (LoginPlayerHelper.isFloodgatePlayer(player)) {
      player.sendMessage(MessageKey.TWO_FACTOR_RECOVERY_BEDROCK.get());
    }
    StringBuilder all = new StringBuilder();
    for (String code : codes) {
      if (all.length() > 0) {
        all.append('\n');
      }
      all.append(code);
    }
    TwoFactorMessageUtil.send(
        player,
        Collections.singletonList(
            new TwoFactorMessageUtil.MessageLine(
                MessageKey.TWO_FACTOR_RECOVERY_COPY_ALL.get(),
                all.toString(),
                MessageKey.TWO_FACTOR_RECOVERY_COPY_ALL_HOVER.get())));
    for (int i = 0; i < codes.size(); i++) {
      final String code = codes.get(i);
      CatScheduler.runTaskLater(
          () ->
              TwoFactorMessageUtil.send(
                  player,
                  Collections.singletonList(
                      new TwoFactorMessageUtil.MessageLine(
                          code, code, MessageKey.TWO_FACTOR_RECOVERY_ITEM_HOVER.get()))),
          (i + 1) * 3L);
    }
  }

  private int digits() {
    int value = Config.Settings.TwoFactorDigits;
    return value > 0 ? value : TotpUtil.DEFAULT_DIGITS;
  }

  private int period() {
    int value = Config.Settings.TwoFactorPeriod;
    return value > 0 ? value : TotpUtil.DEFAULT_PERIOD;
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String alias, String[] args) {
    if (args.length == 1) {
      return TabCompleteUtil.filter(SUB_COMMANDS, args[0]);
    }
    return Collections.emptyList();
  }
}
