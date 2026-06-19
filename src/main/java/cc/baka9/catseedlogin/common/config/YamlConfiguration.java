package cc.baka9.catseedlogin.common.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class YamlConfiguration implements Configuration {

    private final org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
    private Map<String, Object> data = new HashMap<>();
    private final File file;

    public YamlConfiguration(File file) {
        this.file = file;
    }

    public static YamlConfiguration loadConfiguration(File file) {
        YamlConfiguration config = new YamlConfiguration(file);
        if (file != null && file.exists()) {
            try {
                config.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return config;
    }

    @SuppressWarnings("unchecked")
    public void load() throws IOException {
        if (file != null && file.exists()) {
            try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                Object loaded = yaml.load(new InputStreamReader(fis, StandardCharsets.UTF_8));
                if (loaded instanceof Map) {
                    this.data = (Map<String, Object>) loaded;
                }
            }
        }
    }

    public void loadFromResource(InputStream inputStream) {
        if (inputStream != null) {
            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                @SuppressWarnings("unchecked")
                Object loaded = yaml.load(reader);
                if (loaded instanceof Map) {
                    this.data = new HashMap<>((Map<String, Object>) loaded);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void save() throws IOException {
        if (file == null) return;
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (java.io.OutputStreamWriter writer = new OutputStreamWriter(new java.io.FileOutputStream(file), StandardCharsets.UTF_8)) {
            yaml.dump(data, writer);
        }
    }

    public Map<String, Object> getDataMap() {
        return data;
    }

    // ---- Common value parsing ----

    @Override
    @SuppressWarnings("unchecked")
    public boolean getBoolean(String path, boolean defaultValue) {
        Object value = get(path);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }

    private <T> T parseNumeric(String path, T defaultValue, java.util.function.Function<Number, T> numberFn, java.util.function.Function<String, T> stringFn) {
        Object value = getNumeric(path);
        if (value == null) return defaultValue;
        if (value instanceof Number) return numberFn.apply((Number) value);
        try {
            return stringFn.apply((String) value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Object getNumeric(String path) {
        Object value = get(path);
        if (value instanceof Number) return value;
        if (value instanceof String) return value;
        return null;
    }

    @Override
    public int getInt(String path, int defaultValue) {
        return parseNumeric(path, defaultValue, Number::intValue, Integer::parseInt);
    }

    @Override
    public long getLong(String path, long defaultValue) {
        return parseNumeric(path, defaultValue, Number::longValue, Long::parseLong);
    }

    @Override
    public double getDouble(String path, double defaultValue) {
        return parseNumeric(path, defaultValue, Number::doubleValue, Double::parseDouble);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getString(String path, String defaultValue) {
        Object value = get(path);
        return value != null ? String.valueOf(value) : defaultValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getStringList(String path) {
        Object value = get(path);
        if (value instanceof List) {
            return ((List<?>) value).stream()
                    .map(String::valueOf)
                    .collect(java.util.stream.Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public ConfigurationSection getSection(String path) {
        return new ConfigurationSectionImpl(this, path);
    }

    @Override
    public void set(String path, Object value) {
        setNestedValue(data, path.split("\\."), 0, value);
    }

    @Override
    public boolean contains(String path) {
        return get(path) != null;
    }

    @SuppressWarnings("unchecked")
    private Object get(String path) {
        try {
            String[] parts = path.split("\\.");
            Map<String, Object> current = data;
            for (int i = 0; i < parts.length - 1; i++) {
                Object next = current.get(parts[i]);
                if (!(next instanceof Map)) {
                    return null;
                }
                current = (Map<String, Object>) next;
            }
            return current.get(parts[parts.length - 1]);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void setNestedValue(Map<String, Object> map, String[] keys, int index, Object value) {
        try {
            if (index == keys.length - 1) {
                if (value == null) {
                    map.remove(keys[index]);
                } else {
                    map.put(keys[index], value);
                }
                return;
            }
            Object next = map.computeIfAbsent(keys[index], k -> new HashMap<>());
            if (!(next instanceof Map)) {
                next = new HashMap<>();
                map.put(keys[index], next);
            }
            setNestedValue((Map<String, Object>) next, keys, index + 1, value);
        } catch (Exception e) {
            // ignore
        }
    }

    public File getFile() {
        return file;
    }

    private static class ConfigurationSectionImpl implements ConfigurationSection {
        private final Configuration root;
        private final String path;

        ConfigurationSectionImpl(Configuration root, String path) {
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
    }
}
