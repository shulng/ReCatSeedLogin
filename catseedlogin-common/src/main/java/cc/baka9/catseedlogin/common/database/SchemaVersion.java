package cc.baka9.catseedlogin.common.database;

/**
 * Database schema version constants.
 */
public final class SchemaVersion {

    /**
     * Initial schema (v2.x compatible).
     * Columns: name, password, email, ips, lastAction, location
     */
    public static final int V0_INITIAL = 0;

    /**
     * V1: Add password_version column for migration tracking.
     */
    public static final int V1_PASSWORD_VERSION = 1;

    /**
     * V2: Add indexes for performance.
     */
    public static final int V2_INDEXES = 2;

    /**
     * Current schema version.
     */
    public static final int CURRENT = V2_INDEXES;

    private SchemaVersion() {}
}
