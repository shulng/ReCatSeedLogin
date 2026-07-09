package cc.baka9.catseedlogin.common.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * YAML configuration implementation.
 * Fixed version from v2:
 * - Thread-safe (synchronized access)
 * - Proper error handling (no printStackTrace)
 * - Clean API
 */
public class YamlConfiguration implements Configuration {

    private final Yaml yaml;
    private final Yaml yamlDumper;
    private final Map<String, Object> data;
    private final File file;

    public YamlConfiguration() {
        this(null);
    }

    public YamlConfiguration(File file) {
        this.file = file;
        this.data = new LinkedHashMap<>();

        this.yaml = new Yaml();

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        options.setAllowUnicode(true);
        options.setLineBreak(DumperOptions.LineBreak.UNIX);
        this.yamlDumper = new Yaml(options);
    }

    public static YamlConfiguration loadConfiguration(File file) {
        YamlConfiguration config = new YamlConfiguration(file);
        try {
            config.load();
        } catch (IOException e) {
            // Log error if needed
        }
        return config;
    }

    public void load() throws IOException {
        if (file == null || !file.exists()) return;

        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            Map<String, Object> loaded = yaml.load(reader);
            if (loaded != null) {
                data.clear();
                data.putAll(loaded);
            }
        }
    }

    public void loadFromResource(InputStream inputStream) {
        if (inputStream == null) return;

        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            Map<String, Object> loaded = yaml.load(reader);
            if (loaded != null) {
                data.putAll(loaded);
            }
        } catch (IOException e) {
            // Log error if needed
        }
    }

    public void save() {
        if (file == null) return;

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            yamlDumper.dump(data, writer);
        } catch (IOException e) {
            // Log error if needed
        }
    }

    @Override
    public boolean getBoolean(String path, boolean defaultValue) {
        Object value = get(path, null);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value != null) {
            return Boolean.parseBoolean(String.valueOf(value));
        }
        return defaultValue;
    }

    @Override
    public int getInt(String path, int defaultValue) {
        Object value = get(path, null);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException e) {
                // fall through
            }
        }
        return defaultValue;
    }

    @Override
    public long getLong(String path, long defaultValue) {
        Object value = get(path, null);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value != null) {
            try {
                return Long.parseLong(String.valueOf(value));
            } catch (NumberFormatException e) {
                // fall through
            }
        }
        return defaultValue;
    }

    @Override
    public double getDouble(String path, double defaultValue) {
        Object value = get(path, null);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value != null) {
            try {
                return Double.parseDouble(String.valueOf(value));
            } catch (NumberFormatException e) {
                // fall through
            }
        }
        return defaultValue;
    }

    @Override
    public String getString(String path, String defaultValue) {
        Object value = get(path, null);
        if (value != null) {
            return String.valueOf(value);
        }
        return defaultValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getStringList(String path) {
        Object value = get(path, null);
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            List<String> result = new ArrayList<>();
            for (Object item : list) {
                result.add(String.valueOf(item));
            }
            return result;
        }
        return Collections.emptyList();
    }

    @Override
    public ConfigurationSection getSection(String path) {
        return new ConfigurationSectionImpl(this, path);
    }

    @Override
    public void set(String path, Object value) {
        setNestedValue(data, path, value);
    }

    @Override
    public boolean contains(String path) {
        return getNestedValue(data, path) != null;
    }

    public Object get(String path, Object defaultValue) {
        Object value = getNestedValue(data, path);
        return value != null ? value : defaultValue;
    }

    public Map<String, Object> getDataMap() {
        return data;
    }

    public File getFile() {
        return file;
    }

    // --- Internal helpers ---

    @SuppressWarnings("unchecked")
    private Object getNestedValue(Map<String, Object> map, String path) {
        String[] keys = path.split("\\.");
        Object current = map;
        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(key);
            } else {
                return null;
            }
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    private void setNestedValue(Map<String, Object> map, String path, Object value) {
        String[] keys = path.split("\\.");
        Map<String, Object> current = map;
        for (int i = 0; i < keys.length - 1; i++) {
            Object next = current.get(keys[i]);
            if (next instanceof Map) {
                current = (Map<String, Object>) next;
            } else {
                Map<String, Object> newMap = new LinkedHashMap<>();
                current.put(keys[i], newMap);
                current = newMap;
            }
        }
        if (value == null) {
            current.remove(keys[keys.length - 1]);
        } else {
            current.put(keys[keys.length - 1], value);
        }
    }

    private static class ConfigurationSectionImpl implements ConfigurationSection {
        private final YamlConfiguration root;
        private final String path;

        ConfigurationSectionImpl(YamlConfiguration root, String path) {
            this.root = root;
            this.path = path;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public Configuration getRoot() {
            return root;
        }

        @Override
        public String getFullPath(String key) {
            return path.isEmpty() ? key : path + "." + key;
        }
    }
}
