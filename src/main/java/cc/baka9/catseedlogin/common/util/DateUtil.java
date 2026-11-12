package cc.baka9.catseedlogin.common.util;

import java.util.concurrent.ThreadLocalRandom;

public class DateUtil {

  private static final String VERIFICATION_CODE_CHARS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  private static final int DEFAULT_CODE_LENGTH = 10;

  public static String generateVerificationCode() {
    StringBuilder sb = new StringBuilder(DEFAULT_CODE_LENGTH);
    ThreadLocalRandom random = ThreadLocalRandom.current();
    for (int i = 0; i < DEFAULT_CODE_LENGTH; i++) {
      sb.append(VERIFICATION_CODE_CHARS.charAt(random.nextInt(VERIFICATION_CODE_CHARS.length())));
    }
    return sb.toString();
  }

  public static boolean isExpired(long createTime, long durationMillis) {
    return System.currentTimeMillis() - createTime > durationMillis;
  }
}
