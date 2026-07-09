package cc.baka9.catseedlogin.common.crypto;

/**
 * Strategy interface for password hashing.
 * Each implementation handles a specific hash version.
 */
public interface PasswordHasher {

    /**
     * Get the hash version this hasher produces.
     */
    HashVersion getVersion();

    /**
     * Hash a password.
     *
     * @param name the player name (used as salt component in legacy mode)
     * @param password the plaintext password
     * @return the hashed password (with version prefix)
     */
    String hash(String name, String password);

    /**
     * Verify a password against a stored hash.
     *
     * @param name the player name
     * @param password the plaintext password to verify
     * @param storedHash the stored hash to verify against
     * @return true if the password matches
     */
    boolean verify(String name, String password, String storedHash);
}
