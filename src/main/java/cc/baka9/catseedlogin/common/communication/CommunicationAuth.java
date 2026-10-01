package cc.baka9.catseedlogin.common.communication;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** 代理端与登录服之间的 HMAC-SHA256 签名工具，用于校验 {@code KeepLoggedIn} 请求。 */
public final class CommunicationAuth {

  private static final String HMAC_SHA256 = "HmacSHA256";

  private static final ThreadLocal<Mac> MAC =
      ThreadLocal.withInitial(
          () -> {
            try {
              return Mac.getInstance(HMAC_SHA256);
            } catch (NoSuchAlgorithmException e) {
              throw new RuntimeException(e);
            }
          });

  private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

  private CommunicationAuth() {}

  /**
   * 使用 {@code key} 作为密钥，对 {@code data} 拼接后的消息计算 HMAC-SHA256 签名。
   *
   * @param key 共享密钥（配置中的 auth-key）
   * @param data 参与签名的消息片段
   * @return 小写十六进制签名
   */
  public static String encryption(String key, String... data) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("HMAC key must not be null or empty");
    }
    String message = String.join(":", data);
    Mac mac = MAC.get();
    try {
      mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
    } catch (InvalidKeyException e) {
      throw new RuntimeException(e);
    }
    byte[] signature = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
    char[] hex = new char[signature.length * 2];
    for (int i = 0; i < signature.length; i++) {
      int value = signature[i] & 0xff;
      hex[i * 2] = HEX_CHARS[value >>> 4];
      hex[i * 2 + 1] = HEX_CHARS[value & 0x0f];
    }
    return new String(hex);
  }
}
