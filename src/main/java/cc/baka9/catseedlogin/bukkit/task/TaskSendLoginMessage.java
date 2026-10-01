package cc.baka9.catseedlogin.bukkit.task;

import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.session.LoginPlayerHelper;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/** 周期性提醒未登录玩家进行登录或注册。 */
public class TaskSendLoginMessage extends Task {

  @Override
  public void run() {
    if (!Cache.isLoaded) return;

    for (Player player : Bukkit.getOnlinePlayers()) {
      sendLoginMessage(player);
    }
  }

  private void sendLoginMessage(Player player) {
    try {
      String playerName = player.getName();
      if (LoginPlayerHelper.isLogin(playerName)) return;

      player.sendMessage(
          LoginPlayerHelper.isRegister(playerName)
              ? MessageKey.LOGIN_REQUEST.get()
              : MessageKey.REGISTER_REQUEST.get());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
