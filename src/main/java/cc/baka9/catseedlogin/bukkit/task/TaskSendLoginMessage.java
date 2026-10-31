package cc.baka9.catseedlogin.bukkit.task;

import cc.baka9.catseedlogin.bukkit.Cache;
import cc.baka9.catseedlogin.bukkit.Config;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.bukkit.object.TwoFactorLimiter;
import cc.baka9.catseedlogin.bukkit.object.TwoFactorState;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TaskSendLoginMessage extends Task {
  @Override
  public void run() {
    if (!Cache.isLoaded) return;

    // 清理超过窗口没有失败记录的条目，计数超时清理规则的一部分
    TwoFactorLimiter.sweep(Config.Settings.TwoFactorAttemptWindow * 1000L);

    for (Player player : Bukkit.getOnlinePlayers()) {
      sendLoginMessage(player);
    }
  }

  private void sendLoginMessage(Player player) {
    try {
      String playerName = player.getName();
      if (LoginPlayerHelper.isLogin(playerName)) return;

      if (TwoFactorState.isAwaitingTwoFactor(playerName)) {
        // 已经过了密码这一步，别再提示输密码了
        player.sendMessage(MessageKey.TWO_FACTOR_REQUIRED_REMINDER.get());
        return;
      }

      String message =
          LoginPlayerHelper.isRegister(playerName)
              ? Config.Language.LOGIN_REQUEST
              : Config.Language.REGISTER_REQUEST;
      player.sendMessage(message);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
