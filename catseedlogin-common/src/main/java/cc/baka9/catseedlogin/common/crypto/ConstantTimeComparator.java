package cc.baka9.catseedlogin.common.crypto;

/**
 * Timing-safe string comparison to prevent side-channel attacks.
 * Uses XOR-based comparison that takes constant time regardless of where strings differ.
 */
public final class ConstantTimeComparator {

    private ConstantTimeComparator() {}

    /**
     * Compare two strings in constant time.
     * Returns false immediately if lengths differ (length is not secret).
     */
    public static boolean equals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }

        byte[] aBytes = a.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return equals(aBytes, bBytes);
    }

    /**
     * Compare two byte arrays in constant time.
     */
    public static boolean equals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }

        int diff = 0;
        for (int i = 0; i < a.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
