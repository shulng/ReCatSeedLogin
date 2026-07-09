package cc.baka9.catseedlogin.common.crypto;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Argon2id password hasher - current recommended algorithm.
 * Uses Bouncy Castle FIPS provider for Argon2 implementation.
 *
 * Parameters (OWASP 2024 recommendations):
 * - Memory: 65536 KB (64 MB)
 * - Iterations: 3
 * - Parallelism: 4
 * - Salt length: 16 bytes
 * - Hash length: 32 bytes
 */
public class Argon2IdHasher implements PasswordHasher {

    private static final int MEMORY_COST = 65536;
    private static final int ITERATIONS = 3;
    private static final int PARALLELISM = 4;
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;

    private static final String PREFIX = "$v2$";
    private static final String SEPARATOR = "$";

    private final SecureRandom random = new SecureRandom();

    @Override
    public HashVersion getVersion() {
        return HashVersion.V2_ARGON2;
    }

    @Override
    public String hash(String name, String password) {
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);

        byte[] hash = argon2Hash(password, salt);

        return PREFIX
                + encodeBase64(salt) + SEPARATOR
                + encodeBase64(hash);
    }

    @Override
    public boolean verify(String name, String password, String storedHash) {
        if (storedHash == null || !storedHash.startsWith(PREFIX)) {
            return false;
        }

        try {
            String body = storedHash.substring(PREFIX.length());
            String[] parts = body.split("\\$");
            if (parts.length != 2) return false;

            byte[] salt = decodeBase64(parts[0]);
            byte[] expectedHash = decodeBase64(parts[1]);

            byte[] computedHash = argon2Hash(password, salt);

            return ConstantTimeComparator.equals(expectedHash, computedHash);
        } catch (Exception e) {
            return false;
        }
    }

    private byte[] argon2Hash(String password, byte[] salt) {
        Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withMemoryAsKB(MEMORY_COST)
                .withIterations(ITERATIONS)
                .withParallelism(PARALLELISM)
                .withSalt(salt)
                .build();

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(params);

        byte[] hash = new byte[HASH_LENGTH];
        generator.generateBytes(password.getBytes(StandardCharsets.UTF_8), hash, 0, hash.length);
        return hash;
    }

    private String encodeBase64(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private byte[] decodeBase64(String data) {
        return Base64.getUrlDecoder().decode(data);
    }
}
