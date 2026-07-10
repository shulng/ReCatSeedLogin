package cc.baka9.catseedlogin.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class Crypt {

    private static final Argon2 ARGON2 = Argon2Factory.create(
            Argon2Factory.Argon2Types.ARGON2id, 16, 32);
    private static final int ARGON2_ITERATIONS = 4;
    private static final int ARGON2_MEMORY = 65536;
    private static final int ARGON2_PARALLELISM = 2;

    private static final char[] HEX_CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    private static final String ARGON2_PREFIX = "$argon2id$";

    /**
     * Hash a password using Argon2id.
     */
    public static String encrypt(final String name, final String password) {
        return ARGON2.hash(ARGON2_ITERATIONS, ARGON2_MEMORY, ARGON2_PARALLELISM, password.toCharArray());
    }

    /**
     * Verify a password against a stored hash.
     * Supports both Argon2id hashes and legacy SHA-512 hashes.
     * Returns true if the password matches.
     */
    public static boolean match(final String name, final String password, final String encrypted) {
        if (encrypted == null || encrypted.isEmpty()) {
            return false;
        }
        try {
            if (encrypted.startsWith(ARGON2_PREFIX)) {
                return ARGON2.verify(encrypted, password.toCharArray());
            }
            return legacyMatch(name, password, encrypted);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if the stored hash is an Argon2id hash.
     */
    public static boolean isArgon2(String storedHash) {
        return storedHash != null && storedHash.startsWith(ARGON2_PREFIX);
    }

    /**
     * Legacy SHA-512 hash (for backward compatibility with old passwords).
     */
    static String legacyEncrypt(final String name, final String password) {
        String text = "ÜÄaeut//&/=I " + password + "7421€547" + name + "__+IÄIH§%NK " + password;
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(text.getBytes(StandardCharsets.UTF_8), 0, text.length());
            return byteArrayToHexString(md.digest());
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean legacyMatch(String name, String password, String encrypted) {
        return encrypted.equals(legacyEncrypt(name, password));
    }

    public static String byteArrayToHexString(byte[] args) {
        char[] chars = new char[args.length * 2];
        for (int i = 0; i < args.length; i++) {
            chars[(i * 2)] = HEX_CHARS[(args[i] >> 4 & 0xF)];
            chars[(i * 2 + 1)] = HEX_CHARS[(args[i] & 0xF)];
        }
        return new String(chars);
    }
}
