package cc.baka9.catseedlogin.common.database;

import cc.baka9.catseedlogin.api.AccountStorage;
import java.io.Closeable;

/**
 * Database interface for account storage.
 * Platform modules implement this for SQLite/MySQL.
 */
public interface Database extends Closeable, AccountStorage {

    /**
     * Initialize the database (create tables, run migrations).
     */
    void initialize();

    /**
     * Get the current schema version.
     */
    int getSchemaVersion();

    /**
     * Set the schema version.
     */
    void setSchemaVersion(int version);
}
