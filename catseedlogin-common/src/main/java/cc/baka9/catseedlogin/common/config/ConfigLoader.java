package cc.baka9.catseedlogin.common.config;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Focused config loader that handles loading, merging defaults, and migration.
 * Replaces the monolithic BaseConfigManager from v2.
 */
public class ConfigLoader {

    private final File dataFolder;
    private final ResourceProvider resourceProvider;
    private final Logger logger;

    private YamlConfiguration config;

    public interface ResourceProvider {
        InputStream getResourceAsStream(String name);
    }

    public ConfigLoader(File dataFolder, ResourceProvider resourceProvider, Logger logger) {
        this.dataFolder = dataFolder;
        this.resourceProvider = resourceProvider;
        this.logger = logger;
    }

    /**
     * Load config from disk, merge with defaults, migrate if needed.
     */
    public void load() {
        load("config.yml");
    }

    /**
     * Load a specific config file.
     */
    public void load(String fileName) {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File configFile = new File(dataFolder, fileName);

        // Create default if not exists
        if (!configFile.exists()) {
            createDefaultConfig(fileName);
        }

        // Load from disk
        config = YamlConfiguration.loadConfiguration(configFile);

        // Load defaults from bundled resource
        YamlConfiguration defaults = loadDefaults(fileName);
        if (defaults != null) {
            boolean changed = mergeDefaults(config.getDataMap(), defaults.getDataMap());
            if (changed) {
                config.save();
            }
        }

        // Migrate if needed
        ConfigMigrator.migrate(config);
    }

    /**
     * Reload config from disk.
     */
    public void reload() {
        load();
    }

    /**
     * Get the raw configuration for direct access.
     */
    public YamlConfiguration getRaw() {
        return config;
    }

    /**
     * Get a typed value with default.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String path, Class<T> type, T defaultValue) {
        if (config == null) return defaultValue;
        Object value = config.get(path, null);
        if (value == null) return defaultValue;
        if (type.isInstance(value)) return (T) value;
        // Handle type coercion
        if (type == Integer.class && value instanceof Number) {
            return (T) Integer.valueOf(((Number) value).intValue());
        }
        if (type == Long.class && value instanceof Number) {
            return (T) Long.valueOf(((Number) value).longValue());
        }
        if (type == Boolean.class && value instanceof Boolean) {
            return (T) value;
        }
        if (type == String.class) {
            return (T) String.valueOf(value);
        }
        return defaultValue;
    }

    /**
     * Get a string value.
     */
    public String getString(String path, String defaultValue) {
        return get(path, String.class, defaultValue);
    }

    /**
     * Get an int value.
     */
    public int getInt(String path, int defaultValue) {
        return get(path, Integer.class, defaultValue);
    }

    /**
     * Get a long value.
     */
    public long getLong(String path, long defaultValue) {
        return get(path, Long.class, defaultValue);
    }

    /**
     * Get a boolean value.
     */
    public boolean getBoolean(String path, boolean defaultValue) {
        return get(path, Boolean.class, defaultValue);
    }

    /**
     * Get a string list.
     */
    public List<String> getStringList(String path) {
        if (config == null) return Collections.emptyList();
        return config.getStringList(path);
    }

    /**
     * Set a value and save.
     */
    public void set(String path, Object value) {
        if (config == null) return;
        config.set(path, value);
        config.save();
    }

    /**
     * Create default config from bundled resource.
     */
    private void createDefaultConfig(String fileName) {
        if (resourceProvider == null) return;
        InputStream is = resourceProvider.getResourceAsStream(fileName);
        if (is == null) return;

        File target = new File(dataFolder, fileName);
        try (OutputStream os = new FileOutputStream(target)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            if (logger != null) {
                logger.warning("Failed to create default config: " + fileName);
            }
        } finally {
            try { is.close(); } catch (IOException ignored) {}
        }
    }

    /**
     * Load default values from bundled resource.
     */
    private YamlConfiguration loadDefaults(String fileName) {
        if (resourceProvider == null) return null;
        InputStream is = resourceProvider.getResourceAsStream(fileName);
        if (is == null) return null;

        try {
            YamlConfiguration defaults = new YamlConfiguration();
            defaults.loadFromResource(is);
            return defaults;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Merge missing keys from defaults into config.
     * Returns true if any keys were added.
     */
    private boolean mergeDefaults(Map<String, Object> configMap, Map<String, Object> defaultMap) {
        boolean changed = false;
        for (Map.Entry<String, Object> entry : defaultMap.entrySet()) {
            String key = entry.getKey();
            Object defaultValue = entry.getValue();

            if (!configMap.containsKey(key)) {
                configMap.put(key, defaultValue);
                changed = true;
            } else if (defaultValue instanceof Map && configMap.get(key) instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> configSection = (Map<String, Object>) configMap.get(key);
                @SuppressWarnings("unchecked")
                Map<String, Object> defaultSection = (Map<String, Object>) defaultValue;
                if (mergeDefaults(configSection, defaultSection)) {
                    changed = true;
                }
            }
        }
        return changed;
    }
}
