package cc.baka9.catseedlogin.bukkit.communication;

import cc.baka9.catseedlogin.bukkit.cache.PlayerCache;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.bukkit.platform.BukkitContext;
import cc.baka9.catseedlogin.bukkit.scheduler.CatScheduler;
import cc.baka9.catseedlogin.common.communication.BaseCommunication;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import cc.baka9.catseedlogin.common.model.LoginPlayer;
import cc.baka9.catseedlogin.common.util.CommunicationAuth;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Communication extends BaseCommunication {
  private static ServerSocket serverSocket;
  private static final AtomicBoolean connectAuthFailedWarned = new AtomicBoolean();

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
    if (isAuthKeyBlank()) {
      BukkitContext.getLogger().warning(MessageKey.PROXY_AUTH_KEY_NOT_SET.get());
    }
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
      if (!serverSocket.isClosed()) {
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
          String connectTime = bufferedReader.readLine();
          String connectSign = bufferedReader.readLine();
          handleConnectRequest(outputStream, playerName, connectTime, connectSign);
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
    if (playerName == null) return;
    String authKey = BukkitContext.getConfigManager().getAuthKey();
    if (authKey == null || authKey.isEmpty()) return;
    if (!isValidSign(authKey, playerName, time, sign)) return;

    CatScheduler.runTask(
        () -> {
          LoginPlayer lp = PlayerCache.getIgnoreCase(playerName);
          if (lp == null) return;
          LoginPlayerHelper.add(lp);
          Player player = Bukkit.getPlayerExact(playerName);
          if (player != null) {
            player.updateInventory();
          }
        });
  }

  private static void handleConnectRequest(
      OutputStream outputStream, String playerName, String time, String sign) {
    if (playerName == null) {
      writeConnectResult(outputStream, false);
      return;
    }
    String authKey = BukkitContext.getConfigManager().getAuthKey();
    if (authKey != null && !authKey.isEmpty() && !isValidSign(authKey, playerName, time, sign)) {
      warnConnectAuthFailed();
      writeConnectResult(outputStream, false);
      return;
    }
    writeConnectResult(outputStream, LoginPlayerHelper.isLogin(playerName));
  }

  private static boolean isValidSign(String authKey, String playerName, String time, String sign) {
    return sign != null
        && !sign.isEmpty()
        && CommunicationAuth.isTimestampFresh(time)
        && sign.equals(CommunicationAuth.encryption(authKey, playerName, time));
  }

  private static void warnConnectAuthFailed() {
    if (connectAuthFailedWarned.compareAndSet(false, true)) {
      BukkitContext.getLogger().warning(MessageKey.PROXY_CONNECT_AUTH_FAILED.get());
    }
  }

  private static void writeConnectResult(OutputStream outputStream, boolean loggedIn) {
    try {
      outputStream.write(loggedIn ? 1 : 0);
      outputStream.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static boolean isAuthKeyBlank() {
    String authKey = BukkitContext.getConfigManager().getAuthKey();
    return authKey == null || authKey.isEmpty();
  }

  @Override
  protected String getProxyHost() {
    return BukkitContext.getConfigManager().getProxyHost();
  }

  @Override
  protected int getProxyPort() {
    return BukkitContext.getConfigManager().getProxyPort();
  }

  @Override
  protected String getAuthKey() {
    return BukkitContext.getConfigManager().getAuthKey();
  }

  @Override
  protected void logError(String message, Exception e) {
    BukkitContext.getLogger().severe(message);
    e.printStackTrace();
  }

  @Override
  protected void logWarning(String message) {
    BukkitContext.getLogger().warning(message);
  }
}
