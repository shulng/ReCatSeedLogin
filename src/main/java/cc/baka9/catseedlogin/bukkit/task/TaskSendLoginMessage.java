package cc.baka9.catseedlogin.bukkit.task;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import cc.baka9.catseedlogin.bukkit.Config;
import cc.baka9.catseedlogin.bukkit.Cache;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;

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

            String message = LoginPlayerHelper.isRegister(playerName)
                ? Config.Language.LOGIN_REQUEST
                : Config.Language.REGISTER_REQUEST;
            player.sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
