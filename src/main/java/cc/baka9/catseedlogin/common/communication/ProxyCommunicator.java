package cc.baka9.catseedlogin.common.communication;

import cc.baka9.catseedlogin.common.api.ProxyConfig;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * 代理端通信客户端：向 Bukkit 登录服的 {@code CommunicationServer} 发起 Socket 请求， 查询玩家登录状态或保持登录。
 */
public abstract class ProxyCommunicator {

  private final ProxyConfig config;

  protected ProxyCommunicator(ProxyConfig config) {
    this.config = config;
  }

  protected abstract void logError(String message, Exception e);

  protected abstract void logWarning(String message);

  /** 查询玩家是否已登录，返回 1 表示已登录，0 表示未登录或查询失败。 */
  public int sendConnectRequest(String playerName) {
    try (Socket socket = createSocket();
        BufferedWriter writer = createWriter(socket)) {
      writeLine(writer, "Connect");
      writeLine(writer, playerName);
      writer.flush();
      return socket.getInputStream().read();
    } catch (IOException e) {
      logError("Failed to send connect request for player: " + playerName, e);
    }
    return 0;
  }

  /** 通知登录服保持该玩家的登录状态（用于跨子服切换）。 */
  public void sendKeepLoggedInRequest(String playerName) {
    try (Socket socket = createSocket();
        BufferedWriter writer = createWriter(socket)) {
      writeLine(writer, "KeepLoggedIn");
      writeLine(writer, playerName);
      String time = String.valueOf(System.currentTimeMillis());
      writeLine(writer, time);
      writeLine(writer, CommunicationAuth.encryption(config.getAuthKey(), playerName, time));
      writer.flush();
    } catch (IOException e) {
      logError("Failed to send keep logged in request for player: " + playerName, e);
    }
  }

  protected Socket createSocket() throws IOException {
    try {
      return new Socket(config.getProxyHost(), config.getProxyPort());
    } catch (IOException e) {
      logWarning(MessageKey.CHECK_PROXY_CONFIG.get());
      throw e;
    }
  }

  protected BufferedWriter createWriter(Socket socket) throws IOException {
    return new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
  }

  private void writeLine(BufferedWriter writer, String line) throws IOException {
    if (writer == null || line == null) return;
    writer.write(line);
    writer.newLine();
  }
}
