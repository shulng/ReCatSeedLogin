package cc.baka9.catseedlogin.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

public class DateUtil {

    private static final SimpleDateFormat DEFAULT_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String VERIFICATION_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int DEFAULT_CODE_LENGTH = 10;

    public static String formatTime(long timeMillis) {
        synchronized (DEFAULT_FORMAT) {
            return DEFAULT_FORMAT.format(new Date(timeMillis));
        }
    }

    public static String formatTime(long timeMillis, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        synchronized (sdf) {
            return sdf.format(new Date(timeMillis));
        }
    }

    public static String generateVerificationCode() {
        return generateVerificationCode(DEFAULT_CODE_LENGTH);
    }

    public static String generateVerificationCode(int length) {
        if (length <= 0) {
            length = DEFAULT_CODE_LENGTH;
        }
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(VERIFICATION_CODE_CHARS.charAt(random.nextInt(VERIFICATION_CODE_CHARS.length())));
        }
        return sb.toString();
    }

    public static boolean isExpired(long createTime, long durationMillis) {
        return System.currentTimeMillis() - createTime > durationMillis;
    }

    public static long getRemainingTime(long createTime, long durationMillis) {
        long elapsed = System.currentTimeMillis() - createTime;
        return Math.max(0, durationMillis - elapsed);
    }
}
