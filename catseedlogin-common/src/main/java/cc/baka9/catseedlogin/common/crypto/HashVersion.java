package cc.baka9.catseedlogin.common.crypto;

/**
 * Password hash version identifiers.
 */
public enum HashVersion {

    /**
     * Legacy SHA-512 without per-user salt.
     * Used by CatSeedLogin v2.x. Stored without prefix in old databases.
     */
    V1_LEGACY("v1", 0),

    /**
     * Legacy SHA-512 with $v1$ prefix.
     * For migrated hashes that still use the old algorithm.
     */
    V1_PREFIXED("v1", 1),

    /**
     * Argon2id with per-user random salt.
     * Current recommended algorithm.
     */
    V2_ARGON2("v2", 2);

    private final String name;
    private final int dbVersion;

    HashVersion(String name, int dbVersion) {
        this.name = name;
        this.dbVersion = dbVersion;
    }

    public String getName() {
        return name;
    }

    public int getDbVersion() {
        return dbVersion;
    }

    /**
     * Get the prefix used in stored hashes.
     */
    public String getPrefix() {
        return "$" + name + "$";
    }

    /**
     * Detect hash version from stored hash string.
     */
    public static HashVersion detect(String storedHash) {
        if (storedHash == null || storedHash.isEmpty()) {
            return V1_LEGACY;
        }
        if (storedHash.startsWith("$v2$")) {
            return V2_ARGON2;
        }
        if (storedHash.startsWith("$v1$")) {
            return V1_PREFIXED;
        }
        // No prefix = legacy format
        return V1_LEGACY;
    }
}
