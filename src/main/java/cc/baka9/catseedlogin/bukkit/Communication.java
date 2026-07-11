package cc.baka9.catseedlogin.bukkit;

import cc.baka9.catseedlogin.common.util.CommunicationAuth;
import cc.baka9.catseedlogin.common.communication.BaseCommunication;
import cc.baka9.catseedlogin.bukkit.Cache;
import cc.baka9.catseedlogin.common.model.LoginPlayer;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;

public class Communication extends BaseCommunication {
    private static ServerSocket serverSocket;

    public static void socketServerStopAsync() {
        CatScheduler.runTaskAsync(Communication::socketServerStop);
    }

    public static void socketServerStop() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void socketServerStartAsync() {
        CatScheduler.runTaskAsync(Communication::socketServerStart);
    }

    public static void socketServerStart() {
        try {
            serverSocket = new ServerSocket(PluginContext.getConfigManager().getProxyPort(), 50);
            while (!serverSocket.isClosed()) {
                acceptAndHandle();
            }
        } catch (IOException e) {
            PluginContext.getLogger().warning("无法启动Socket服务器: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void acceptAndHandle() {
        try (Socket socket = serverSocket.accept()) {
            handleRequest(socket);
        } catch (IOException e) {
            if (!serverSocket.isClosed()) {
                PluginContext.getLogger().warning("Socket连接处理异常: " + e.getMessage());
            }
        }
    }

    private static void handleRequest(Socket socket) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             OutputStream outputStream = socket.getOutputStream()) {
            String requestType = bufferedReader.readLine();
            if (requestType == null) return;
            String playerName = bufferedReader.readLine();
            switch (requestType) {
                case "Connect":
                    handleConnectRequest(outputStream, playerName);
                    break;
                case "KeepLoggedIn":
                    String time = bufferedReader.readLine();
                    String sign = bufferedReader.readLine();
                    handleKeepLoggedInRequest(playerName, time, sign);
                    break;
                default:
                    break;
            }
        }
    }

    private static void handleKeepLoggedInRequest(String playerName, String time, String sign) {
        if (playerName == null || time == null || sign == null) return;
        String expectedSign = CommunicationAuth.encryption(
                playerName, time, PluginContext.getConfigManager().getAuthKey());
        if (!MessageDigest.isEqual(sign.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                expectedSign.getBytes(java.nio.charset.StandardCharsets.UTF_8))) return;

        CatScheduler.runTask(() -> {
            LoginPlayer lp = Cache.getIgnoreCase(playerName);
            if (lp == null) return;
            LoginPlayerHelper.add(lp);
            Player player = Bukkit.getPlayerExact(playerName);
            if (player != null) {
                player.updateInventory();
            }
        });
    }

    private static void handleConnectRequest(OutputStream outputStream, String playerName) {
        boolean result = LoginPlayerHelper.isLogin(playerName);
        try {
            outputStream.write(result ? 1 : 0);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getProxyHost() {
        return PluginContext.getConfigManager().getProxyHost();
    }

    @Override
    protected int getProxyPort() {
        return PluginContext.getConfigManager().getProxyPort();
    }

    @Override
    protected String getAuthKey() {
        return PluginContext.getConfigManager().getAuthKey();
    }

    @Override
    protected void logError(String message, Exception e) {
        PluginContext.getLogger().severe(message);
        e.printStackTrace();
    }

    @Override
    protected void logWarning(String message) {
        PluginContext.getLogger().warning(message);
    }
}
