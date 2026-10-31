package cc.baka9.catseedlogin.common.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * TOTP (RFC 6238) 实现，基于 HMAC-SHA1，不依赖任何第三方库。
 *
 * <p>默认参数与常见验证器一致：30 秒步长、6 位数字、SHA1。
 *
 * <p>校验时允许 ±1 个时间步的偏移以容忍时钟误差。注意：{@link #verify} 只要任意一个时间步命中
 * 就返回 true，调用方应当把「一次调用返回 false」记为一次失败，而不是每个时间步各记一次——
 * 否则恰好卡在步长边界上的正常玩家会被快速推到锁定线。
 */
public final class TotpUtil {

  /** 默认时间步长 (秒)。 */
  public static final int DEFAULT_PERIOD = 30;

  /** 默认验证码位数。 */
  public static final int DEFAULT_DIGITS = 6;

  /** 校验时默认允许的时间步偏移。 */
  public static final int DEFAULT_WINDOW = 1;

  /** 密钥长度 (字节)，160 位，与常见验证器一致。 */
  private static final int SECRET_BYTES = 20;

  private static final String HMAC_ALGORITHM = "HmacSHA1";

  /** RFC 4648 Base32 字母表。 */
  private static final char[] BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();

  private static final SecureRandom RANDOM = new SecureRandom();

  private TotpUtil() {}

  /** 生成一个新的随机 Base32 密钥。 */
  public static String generateSecret() {
    byte[] bytes = new byte[SECRET_BYTES];
    RANDOM.nextBytes(bytes);
    return base32Encode(bytes);
  }

  /**
   * 生成验证器扫码用的 otpauth URI。
   *
   * @param issuer 验证器中显示的服务名
   * @param accountName 验证器中显示的账号名
   * @param secret Base32 密钥
   */
  public static String buildOtpAuthUri(String issuer, String accountName, String secret) {
    String safeIssuer = issuer == null ? "" : issuer;
    String safeAccount = accountName == null ? "" : accountName;
    return "otpauth://totp/"
        + urlEncode(safeIssuer)
        + ":"
        + urlEncode(safeAccount)
        + "?secret="
        + (secret == null ? "" : secret)
        + "&issuer="
        + urlEncode(safeIssuer);
  }

  /**
   * 计算指定时刻的验证码。
   *
   * @return 6 位验证码，密钥非法时返回 null
   */
  public static String generateCode(String secret, long timeMillis) {
    return generateCode(secret, timeMillis, DEFAULT_DIGITS, DEFAULT_PERIOD);
  }

  /**
   * 计算指定时刻的验证码。
   *
   * @param digits 验证码位数
   * @param period 时间步长 (秒)
   * @return 验证码，密钥非法时返回 null
   */
  public static String generateCode(String secret, long timeMillis, int digits, int period) {
    byte[] key = base32Decode(secret);
    if (key == null || digits <= 0 || period <= 0) {
      return null;
    }
    return format(hotp(key, counterAt(timeMillis, period)), digits);
  }

  /** 校验验证码，允许 ±1 个时间步偏移。 */
  public static boolean verify(String secret, String code, long timeMillis) {
    return verify(secret, code, timeMillis, DEFAULT_DIGITS, DEFAULT_PERIOD, DEFAULT_WINDOW);
  }

  /**
   * 校验验证码。
   *
   * <p>只要 window 范围内的任意一个时间步命中即返回 true，整次调用只应按一次失败计数。
   */
  public static boolean verify(
      String secret, String code, long timeMillis, int digits, int period, int window) {
    if (code == null) {
      return false;
    }
    String expected = code.trim();
    if (expected.isEmpty()) {
      return false;
    }
    byte[] key = base32Decode(secret);
    if (key == null || digits <= 0 || period <= 0) {
      return false;
    }
    long counter = counterAt(timeMillis, period);
    for (int offset = -window; offset <= window; offset++) {
      if (format(hotp(key, counter + offset), digits).equals(expected)) {
        return true;
      }
    }
    return false;
  }

  private static long counterAt(long timeMillis, int period) {
    return timeMillis / 1000L / period;
  }

  /** RFC 4226 HOTP：HMAC-SHA1 + 动态截断，失败返回 -1。 */
  private static int hotp(byte[] key, long counter) {
    byte[] hash = hmacSha1(key, toCounterBytes(counter));
    if (hash == null || hash.length < 20) {
      return -1;
    }
    int offset = hash[hash.length - 1] & 0x0F;
    return ((hash[offset] & 0x7F) << 24)
        | ((hash[offset + 1] & 0xFF) << 16)
        | ((hash[offset + 2] & 0xFF) << 8)
        | (hash[offset + 3] & 0xFF);
  }

  private static byte[] toCounterBytes(long counter) {
    byte[] bytes = new byte[8];
    for (int i = 7; i >= 0; i--) {
      bytes[i] = (byte) (counter & 0xFF);
      counter >>>= 8;
    }
    return bytes;
  }

  private static byte[] hmacSha1(byte[] key, byte[] message) {
    try {
      Mac mac = Mac.getInstance(HMAC_ALGORITHM);
      mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
      return mac.doFinal(message);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      return null;
    }
  }

  private static String format(int value, int digits) {
    if (value < 0) {
      return "";
    }
    int mod = 1;
    for (int i = 0; i < digits; i++) {
      mod *= 10;
    }
    String number = Integer.toString(value % mod);
    StringBuilder sb = new StringBuilder(digits);
    for (int i = number.length(); i < digits; i++) {
      sb.append('0');
    }
    return sb.append(number).toString();
  }

  private static String base32Encode(byte[] bytes) {
    StringBuilder sb = new StringBuilder((bytes.length * 8 + 4) / 5);
    int buffer = 0;
    int bitsLeft = 0;
    for (byte b : bytes) {
      buffer = (buffer << 8) | (b & 0xFF);
      bitsLeft += 8;
      while (bitsLeft >= 5) {
        sb.append(BASE32_CHARS[(buffer >>> (bitsLeft - 5)) & 0x1F]);
        bitsLeft -= 5;
      }
    }
    if (bitsLeft > 0) {
      sb.append(BASE32_CHARS[(buffer << (5 - bitsLeft)) & 0x1F]);
    }
    return sb.toString();
  }

  /**
   * 解码 Base32 密钥，容忍小写、空格、连字符与结尾填充。
   *
   * @return 解码后的字节，输入非法时返回 null
   */
  private static byte[] base32Decode(String secret) {
    if (secret == null) {
      return null;
    }
    String normalized =
        secret.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT).replaceAll("=+$", "");
    if (normalized.isEmpty()) {
      return null;
    }
    byte[] out = new byte[normalized.length() * 5 / 8];
    int index = 0;
    int buffer = 0;
    int bitsLeft = 0;
    for (char c : normalized.toCharArray()) {
      int value = base32Value(c);
      if (value < 0) {
        return null;
      }
      buffer = (buffer << 5) | value;
      bitsLeft += 5;
      if (bitsLeft >= 8) {
        out[index++] = (byte) ((buffer >>> (bitsLeft - 8)) & 0xFF);
        bitsLeft -= 8;
      }
    }
    return index == out.length ? out : Arrays.copyOf(out, index);
  }

  private static int base32Value(char c) {
    if (c >= 'A' && c <= 'Z') {
      return c - 'A';
    }
    if (c >= '2' && c <= '7') {
      return c - '2' + 26;
    }
    return -1;
  }

  /** 只转义 otpauth URI 里 label / issuer 需要转义的字符。 */
  private static String urlEncode(String value) {
    StringBuilder sb = new StringBuilder(value.length());
    for (byte b : value.getBytes(StandardCharsets.UTF_8)) {
      int unsigned = b & 0xFF;
      char c = (char) unsigned;
      boolean safe =
          (c >= 'a' && c <= 'z')
              || (c >= 'A' && c <= 'Z')
              || (c >= '0' && c <= '9')
              || c == '-'
              || c == '.'
              || c == '_'
              || c == '~';
      if (safe) {
        sb.append(c);
      } else {
        sb.append('%').append(String.format("%02X", unsigned));
      }
    }
    return sb.toString();
  }
}
