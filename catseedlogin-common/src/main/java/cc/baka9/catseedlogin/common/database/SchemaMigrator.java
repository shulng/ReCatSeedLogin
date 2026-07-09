package cc.baka9.catseedlogin.common.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * Handles database schema migrations.
 * Runs DDL statements to upgrade schema incrementally.
 */
public class SchemaMigrator {

    private final Connection connection;
    private final Logger logger;

    public SchemaMigrator(Connection connection, Logger logger) {
        this.connection = connection;
        this.logger = logger;
    }

    /**
     * Run all pending migrations.
     */
    public void runMigrations() {
        int currentVersion = getCurrentVersion();

        if (currentVersion < SchemaVersion.V1_PASSWORD_VERSION) {
            migrateToV1();
        }
        if (currentVersion < SchemaVersion.V2_INDEXES) {
            migrateToV2();
        }
    }

    private int getCurrentVersion() {
        try (Statement stmt = connection.createStatement()) {
            // Ensure schema_info table exists
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS schema_info (version INTEGER NOT NULL DEFAULT 0)"
            );

            java.sql.ResultSet rs = stmt.executeQuery("SELECT version FROM schema_info LIMIT 1");
            if (rs.next()) {
                return rs.getInt("version");
            }
            // No row yet - insert default
            stmt.executeUpdate("INSERT INTO schema_info (version) VALUES (0)");
            return 0;
        } catch (SQLException e) {
            log("Failed to get schema version: " + e.getMessage());
            return 0;
        }
    }

    private void setVersion(int version) {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("UPDATE schema_info SET version = " + version);
        } catch (SQLException e) {
            log("Failed to set schema version: " + e.getMessage());
        }
    }

    /**
     * V0 -> V1: Add password_version column to accounts table.
     */
    private void migrateToV1() {
        log("Migrating database schema V0 -> V1 (adding password_version column)");
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(
                "ALTER TABLE accounts ADD COLUMN password_version INTEGER DEFAULT 0"
            );
            setVersion(SchemaVersion.V1_PASSWORD_VERSION);
            log("Database migration V1 complete");
        } catch (SQLException e) {
            // Column might already exist
            if (!e.getMessage().contains("duplicate column")) {
                log("Migration V1 failed: " + e.getMessage());
            } else {
                setVersion(SchemaVersion.V1_PASSWORD_VERSION);
            }
        }
    }

    /**
     * V1 -> V2: Add indexes for performance.
     */
    private void migrateToV2() {
        log("Migrating database schema V1 -> V2 (adding indexes)");
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(
                "CREATE INDEX IF NOT EXISTS idx_accounts_name ON accounts(name)"
            );
            stmt.executeUpdate(
                "CREATE INDEX IF NOT EXISTS idx_accounts_email ON accounts(email)"
            );
            setVersion(SchemaVersion.V2_INDEXES);
            log("Database migration V2 complete");
        } catch (SQLException e) {
            log("Migration V2 failed: " + e.getMessage());
        }
    }

    private void log(String message) {
        if (logger != null) {
            logger.info("[SchemaMigrator] " + message);
        }
    }
}
