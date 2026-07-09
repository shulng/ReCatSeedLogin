package cc.baka9.catseedlogin.common.crypto;

/**
 * Handles password version detection and migration.
 * On successful login with old hash, transparently re-hashes with Argon2id.
 */
public final class PasswordMigrator {

    private static final LegacySha512Hasher LEGACY_HASHER = new LegacySha512Hasher();
    private static final Argon2IdHasher ARGON2_HASHER = new Argon2IdHasher();

    private PasswordMigrator() {}

    /**
     * Result of a password verification attempt.
     */
    public static class VerifyResult {
        private final boolean success;
        private final boolean needsMigration;
        private final String newHash;

        private VerifyResult(boolean success, boolean needsMigration, String newHash) {
            this.success = success;
            this.needsMigration = needsMigration;
            this.newHash = newHash;
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean needsMigration() {
            return needsMigration;
        }

        /**
         * Get the new Argon2id hash if migration is needed.
         * Returns null if no migration is needed.
         */
        public String getNewHash() {
            return newHash;
        }
    }

    /**
     * Verify a password against a stored hash, detecting the version automatically.
     * If verification succeeds with an old algorithm, returns the new Argon2id hash.
     *
     * @param name the player name
     * @param password the plaintext password
     * @param storedHash the stored hash (may or may not have version prefix)
     * @return verification result with migration info
     */
    public static VerifyResult verifyAndMigrate(String name, String password, String storedHash) {
        HashVersion version = HashVersion.detect(storedHash);

        boolean success;
        boolean needsMigration = false;
        String newHash = null;

        switch (version) {
            case V1_LEGACY:
            case V1_PREFIXED:
                success = LEGACY_HASHER.verify(name, password, storedHash);
                if (success) {
                    needsMigration = true;
                    newHash = ARGON2_HASHER.hash(name, password);
                }
                break;
            case V2_ARGON2:
                success = ARGON2_HASHER.verify(name, password, storedHash);
                break;
            default:
                success = false;
                break;
        }

        return new VerifyResult(success, needsMigration, newHash);
    }

    /**
     * Get the appropriate hasher for a given version.
     */
    public static PasswordHasher getHasher(HashVersion version) {
        switch (version) {
            case V1_LEGACY:
            case V1_PREFIXED:
                return LEGACY_HASHER;
            case V2_ARGON2:
                return ARGON2_HASHER;
            default:
                throw new IllegalArgumentException("Unknown hash version: " + version);
        }
    }

    /**
     * Get the default hasher (Argon2id).
     */
    public static PasswordHasher getDefaultHasher() {
        return ARGON2_HASHER;
    }
}
