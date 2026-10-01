package cc.baka9.catseedlogin.bukkit.communication;

import cc.baka9.catseedlogin.bukkit.BukkitContext;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.scheduler.CatScheduler;
import cc.baka9.catseedlogin.bukkit.session.LoginPlayerHelper;
import cc.baka9.catseedlogin.common.communication.CommunicationAuth;
import cc.baka9.catseedlogin.common.model.LoginPlayer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * 登录服端的 Socket 服务：接收代理端（BungeeCord / Velocity）的连接请求， 回答玩家登录状态并处理 KeepLoggedIn 保活。
 */
public final class CommunicationServer {

  private static ServerSocket serverSocket;

  private CommunicationServer() {}

  public static void stopAsync() {
    CatScheduler.runTaskAsync(CommunicationServer::stop);
  }

  public static void stop() {
    if (serverSocket != null && !serverSocket.isClosed()) {
      try {
        serverSocket.close();
      } catch (IOException e) {
        BukkitContext.getLogger().warning("关闭Socket服务器时出错: " + e.getMessage());
      }
    }
  }

  public static void startAsync() {
    CatScheduler.runTaskAsync(CommunicationServer::start);
  }

  public static void start() {
    try {
      serverSocket = new ServerSocket(BukkitContext.getConfigManager().getProxyPort(), 50);
      while (!serverSocket.isClosed()) {
        acceptAndHandle();
      }
    } catch (IOException e) {
      BukkitContext.getLogger().warning("无法启动Socket服务器: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void acceptAndHandle() {
    try (Socket socket = serverSocket.accept()) {
      handleRequest(socket);
    } catch (IOException e) {
      if (serverSocket != null && !serverSocket.isClosed()) {
        BukkitContext.getLogger().warning("Socket连接处理异常: " + e.getMessage());
      }
    }
  }

  private static void handleRequest(Socket socket) throws IOException {
    try (BufferedReader bufferedReader =
            new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
    String expectedSign =
        CommunicationAuth.encryption(
            BukkitContext.getConfigManager().getAuthKey(), playerName, time);
    if (!sign.equals(expectedSign)) return;

    CatScheduler.runTask(
        () -> {
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
      BukkitContext.getLogger().warning("响应代理端登录查询失败: " + e.getMessage());
    }
  }
}
