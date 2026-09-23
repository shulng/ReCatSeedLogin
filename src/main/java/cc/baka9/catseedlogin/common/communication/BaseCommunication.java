package cc.baka9.catseedlogin.common.communication;

import cc.baka9.catseedlogin.common.i18n.MessageKey;
import cc.baka9.catseedlogin.common.util.CommunicationAuth;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseCommunication {

  private final AtomicBoolean authKeyMissingWarned = new AtomicBoolean();

  protected abstract String getProxyHost();

  protected abstract int getProxyPort();

  protected abstract void logError(String message, Exception e);

  protected abstract void logWarning(String message);

  /**
   * 发送登录态查询请求。已配置 auth-key 时附带时间戳与 HMAC 签名,未配置时以空签名发送(由服务端决定是否放行)。
   *
   * @param playerName 待查询的玩家名
   * @return 服务端返回的登录态(1 已登录 / 0 未登录或校验失败)
   */
  public int sendConnectRequest(String playerName) {
    String authKey = getAuthKey();
    boolean signed = authKey != null && !authKey.isEmpty();
    if (!signed) {
      warnAuthKeyMissing();
    }
    String time = String.valueOf(System.currentTimeMillis());
    String sign = signed ? CommunicationAuth.encryption(authKey, playerName, time) : "";
    try (Socket socket = createSocket();
        BufferedWriter writer = createWriter(socket)) {
      writeLine(writer, "Connect");
      writeLine(writer, playerName);
      writeLine(writer, time);
      writeLine(writer, sign);
      writer.flush();
      return socket.getInputStream().read();
    } catch (IOException e) {
      logError("Failed to send connect request for player: " + playerName, e);
    }
    return 0;
  }

  public void sendKeepLoggedInRequest(String playerName) {
    String authKey = getAuthKey();
    if (authKey == null || authKey.isEmpty()) {
      warnAuthKeyMissing();
      return;
    }
    try (Socket socket = createSocket();
        BufferedWriter writer = createWriter(socket)) {
      writeLine(writer, "KeepLoggedIn");
      writeLine(writer, playerName);
      String time = String.valueOf(System.currentTimeMillis());
      writeLine(writer, time);
      String sign = CommunicationAuth.encryption(authKey, playerName, time);
      writeLine(writer, sign);
      writer.flush();
    } catch (IOException e) {
      logError("Failed to send keep logged in request for player: " + playerName, e);
    }
  }

  protected abstract String getAuthKey();

  private void warnAuthKeyMissing() {
    if (authKeyMissingWarned.compareAndSet(false, true)) {
      logWarning(MessageKey.PROXY_AUTH_KEY_NOT_SET.get());
    }
  }

  protected Socket createSocket() throws IOException {
    try {
      return new Socket(getProxyHost(), getProxyPort());
    } catch (IOException e) {
      logWarning(MessageKey.CHECK_PROXY_CONFIG.get());
      throw new IOException(e);
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
