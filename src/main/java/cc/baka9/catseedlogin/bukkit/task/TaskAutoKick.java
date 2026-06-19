package cc.baka9.catseedlogin.bukkit.task;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import cc.baka9.catseedlogin.bukkit.Config;
import cc.baka9.catseedlogin.bukkit.Cache;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;

public class TaskAutoKick extends Task {
    private final Map<String, Long> playerJoinTime = new ConcurrentHashMap<>();

    @Override
    public void run() {
        if (!Cache.isLoaded || Config.Settings.AutoKick < 1) return;

        long autoKickMs = Config.Settings.AutoKick * 1000L;
        long now = System.currentTimeMillis();

        for (Player player : Bukkit.getOnlinePlayers()) {
            checkAndKickPlayer(player, now, autoKickMs);
        }
    }

    public void removePlayer(String playerName) {
        playerJoinTime.remove(playerName);
    }

    private void checkAndKickPlayer(Player player, long now, long autoKickMs) {
        String playerName = player.getName();
        try {
            if (LoginPlayerHelper.isLogin(playerName)) {
                playerJoinTime.remove(playerName);
                return;
            }
            checkAndKickTimeoutPlayer(player, now, autoKickMs);
        } catch (Exception e) {
            playerJoinTime.remove(playerName);
            e.printStackTrace();
        }
    }

    private void checkAndKickTimeoutPlayer(Player player, long now, long autoKickMs) {
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
        String kickMessage = Config.Language.AUTO_KICK
                .replace("{time}", String.valueOf(Config.Settings.AutoKick));
        player.kickPlayer(kickMessage);
    }
}
