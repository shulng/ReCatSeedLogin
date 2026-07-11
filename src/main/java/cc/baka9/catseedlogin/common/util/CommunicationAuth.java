package cc.baka9.catseedlogin.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class CommunicationAuth {

    public static String encryption(String... args) {
        String paramString = String.join("", args);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] arrayOfByte = md.digest(paramString.getBytes(StandardCharsets.UTF_8));
            StringBuilder stringBuilder = new StringBuilder(arrayOfByte.length * 2);
            for (byte value : arrayOfByte) {
                stringBuilder.append(String.format("%02x", value & 0xff));
            }
            return stringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}