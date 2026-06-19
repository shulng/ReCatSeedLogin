package cc.baka9.catseedlogin.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CommunicationAuth {

    private static final ThreadLocal<MessageDigest> MESSAGE_DIGEST = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    });

    public static String encryption(String... args) {
        String paramString = String.join("", args);
        MessageDigest md = MESSAGE_DIGEST.get();
        md.reset();
        byte[] arrayOfByte = md.digest(paramString.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder stringBuilder = new StringBuilder(arrayOfByte.length * 2);
        for (byte value : arrayOfByte) {
            stringBuilder.append(String.format("%02x", value & 0xff));
        }
        MESSAGE_DIGEST.remove();
        return stringBuilder.toString();
    }
}