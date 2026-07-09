package cc.baka9.catseedlogin.common.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Legacy SHA-512 password hasher for backward compatibility.
 * Replicates the exact algorithm from CatSeedLogin v2.x Crypt.java.
 *
 * IMPORTANT: This hasher has known security weaknesses:
 * - No per-user random salt (static hardcoded "salt" strings)
 * - Password appears twice in hash input
 * - Used only for reading existing passwords during migration
 */
public class LegacySha512Hasher implements PasswordHasher {

    private static final char[] HEX_CHARS = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'a', 'b', 'c', 'd', 'e', 'f'
    };

    @Override
    public HashVersion getVersion() {
        return HashVersion.V1_LEGACY;
    }

    @Override
    public String hash(String name, String password) {
        String text = "\u00dc\u00c4aeut//&/=I " + password + "7421\u20ac547" + name + "__+I\u00c4IH\u00a7%NK " + password;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            md.update(bytes, 0, bytes.length);
            return byteArrayToHexString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-512 not available", e);
        }
    }

    @Override
    public boolean verify(String name, String password, String storedHash) {
        if (storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        // Strip prefix if present
        String cleanHash = stripPrefix(storedHash);
        String computed = hash(name, password);
        return ConstantTimeComparator.equals(cleanHash, computed);
    }

    /**
     * Strip the version prefix from a stored hash.
     * Returns the raw hash without prefix.
     */
    static String stripPrefix(String storedHash) {
        if (storedHash.startsWith("$v1$")) {
            return storedHash.substring(4);
        }
        return storedHash;
    }

    /**
     * Convert bytes to hex string.
     */
    static String byteArrayToHexString(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            chars[i * 2] = HEX_CHARS[(bytes[i] >> 4) & 0xF];
            chars[i * 2 + 1] = HEX_CHARS[bytes[i] & 0xF];
        }
        return new String(chars);
    }
}
