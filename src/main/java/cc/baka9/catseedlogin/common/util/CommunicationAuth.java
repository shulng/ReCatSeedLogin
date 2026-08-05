package cc.baka9.catseedlogin.common.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class CommunicationAuth {

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

  /**
   * Computes an HMAC-SHA256 signature over the joined {@code data} using {@code key} as the secret.
   *
   * @param key the shared secret (auth-key) used to authenticate the message
   * @param data the message parts to be joined and signed
   * @return the signature as a lowercase hex string
   */
  public static String encryption(String key, String... data) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("HMAC key must not be null or empty");
    }
    String message = String.join("", data);
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
