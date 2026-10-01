package cc.baka9.catseedlogin.bukkit.task;

import cc.baka9.catseedlogin.bukkit.BukkitContext;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.session.LoginPlayerHelper;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/** 自动踢出超时未登录的玩家。 */
public class TaskAutoKick extends Task {

  private final Map<String, Long> playerJoinTime = new ConcurrentHashMap<>();

  @Override
  public void run() {
    int autoKickSeconds = BukkitContext.getConfigManager().getAutoKick();
    if (!Cache.isLoaded || autoKickSeconds < 1) return;

    long autoKickMs = autoKickSeconds * 1000L;
    long now = System.currentTimeMillis();

    for (Player player : Bukkit.getOnlinePlayers()) {
      checkAndKickPlayer(player, now, autoKickMs, autoKickSeconds);
    }
  }

  public void removePlayer(String playerName) {
    playerJoinTime.remove(playerName);
  }

  private void checkAndKickPlayer(Player player, long now, long autoKickMs, int autoKickSeconds) {
    String playerName = player.getName();
    try {
      if (LoginPlayerHelper.isLogin(playerName)) {
        playerJoinTime.remove(playerName);
        return;
      }
      checkAndKickTimeoutPlayer(player, now, autoKickMs, autoKickSeconds);
    } catch (Exception e) {
      playerJoinTime.remove(playerName);
      e.printStackTrace();
    }
  }

  private void checkAndKickTimeoutPlayer(
      Player player, long now, long autoKickMs, int autoKickSeconds) {
    String playerName = player.getName();
    playerJoinTime.putIfAbsent(playerName, now);
    Long joinTime = playerJoinTime.get(playerName);
    if (joinTime == null || now - joinTime <= autoKickMs) {
      return;
    }
    if (!player.isOnline()) {
      playerJoinTime.remove(playerName);
      return;
    }
    player.kickPlayer(
        MessageKey.AUTO_KICK.get().replace("{time}", String.valueOf(autoKickSeconds)));
  }
}
