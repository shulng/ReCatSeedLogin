package cc.baka9.catseedlogin.common.config;

import java.util.Map;

/**
 * Handles migration from old config format (v2.x) to new format (v3.0).
 * Runs on first load after upgrade.
 */
public final class ConfigMigrator {

    private ConfigMigrator() {}

    /**
     * Migrate config if needed.
     * Returns true if migration was performed.
     */
    public static boolean migrate(YamlConfiguration config) {
        int currentVersion = getConfigVersion(config);

        if (currentVersion >= ConfigSchema.CURRENT_VERSION) {
            return false;
        }

        // Run migrations sequentially
        if (currentVersion < 1) {
            migrateV0ToV1(config);
        }

        // Update version
        config.set(ConfigSchema.KEY_CONFIG_VERSION, ConfigSchema.CURRENT_VERSION);
        config.save();
        return true;
    }

    private static int getConfigVersion(YamlConfiguration config) {
        Object version = config.get(ConfigSchema.KEY_CONFIG_VERSION, null);
        if (version instanceof Number) {
            return ((Number) version).intValue();
        }
        // No version key = old format = version 0
        return 0;
    }

    /**
     * Migration V0 -> V1:
     * - Rename database.mysql (boolean) to database.type (string: "mysql" or "sqlite")
     * - Add password.hash-algorithm key
     * - Ensure all new keys exist via default merge
     */
    private static void migrateV0ToV1(YamlConfiguration config) {
        Map<String, Object> data = config.getDataMap();

        // Migrate database type
        Object mysqlFlag = data.remove("database.mysql");
        if (mysqlFlag != null) {
            boolean isMysql = Boolean.TRUE.equals(mysqlFlag) ||
                    "true".equalsIgnoreCase(String.valueOf(mysqlFlag));
            data.put("database.type", isMysql ? "mysql" : "sqlite");
        }

        // Add password config section if missing
        if (!data.containsKey(ConfigSchema.KEY_PASSWORD_ALGORITHM)) {
            data.put(ConfigSchema.KEY_PASSWORD_ALGORITHM, ConfigSchema.DEFAULT_PASSWORD_ALGORITHM);
        }
    }
}
