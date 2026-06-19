package cc.baka9.catseedlogin.common.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class YamlConfiguration implements Configuration {

    private final org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
    private Map<String, Object> data = new HashMap<>();
    private final File file;
    private List<String> originalLines = new ArrayList<>();

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
            originalLines.clear();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    originalLines.add(line);
                }
            }
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

        if (originalLines.isEmpty()) {
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                yaml.dump(data, writer);
            }
            return;
        }

        Map<String, String> flatData = new LinkedHashMap<>();
        flattenData(data, "", flatData);

        Deque<String> sectionStack = new ArrayDeque<>();
        Set<String> replacedPaths = new LinkedHashSet<>();
        List<String> output = new ArrayList<>();
        List<String> pendingListInserts = null;
        String pendingListPath = null;
        int pendingListIndent = 0;

        for (String line : originalLines) {
            String trimmed = line.trim();

            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                output.add(line);
                continue;
            }

            if (trimmed.startsWith("- ")) {
                if (pendingListInserts != null) {
                    continue;
                }
                output.add(line);
                continue;
            }

            if (pendingListInserts != null) {
                for (String newItem : pendingListInserts) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < pendingListIndent; i++) sb.append(' ');
                    sb.append("- ").append(newItem);
                    output.add(sb.toString());
                }
                pendingListInserts = null;
                pendingListPath = null;
            }

            int indent = 0;
            while (indent < line.length() && line.charAt(indent) == ' ') indent++;
            int level = indent / 2;

            while (sectionStack.size() > level) {
                sectionStack.removeLast();
            }

            int colonIdx = trimmed.indexOf(':');
            if (colonIdx <= 0) {
                output.add(line);
                continue;
            }

            String key = trimmed.substring(0, colonIdx).trim();
            String valueStr = colonIdx < trimmed.length() - 1 ? trimmed.substring(colonIdx + 1).trim() : "";

            StringBuilder pathBuilder = new StringBuilder();
            for (String s : sectionStack) {
                if (pathBuilder.length() > 0) pathBuilder.append(".");
                pathBuilder.append(s);
            }
            if (pathBuilder.length() > 0) pathBuilder.append(".");
            pathBuilder.append(key);
            String fullPath = pathBuilder.toString();

            if (valueStr.isEmpty()) {
                sectionStack.addLast(key);
                output.add(line);
                continue;
            }

            String newValue = flatData.get(fullPath);
            if (newValue != null && !formatRawValue(valueStr).equals(formatRawValue(newValue))) {
                replacedPaths.add(fullPath);
                String prefix = line.substring(0, indent + key.length() + 1);
                output.add(prefix + " " + newValue);

                if (newValue.startsWith("[")) {
                    pendingListPath = fullPath;
                    pendingListIndent = indent + 2;
                }
            } else if (newValue != null) {
                replacedPaths.add(fullPath);
                output.add(line);
            } else {
                output.add(line);
            }
        }

        if (pendingListInserts != null) {
            for (String newItem : pendingListInserts) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < pendingListIndent; i++) sb.append(' ');
                sb.append("- ").append(newItem);
                output.add(sb.toString());
            }
        }

        List<Map.Entry<String, String>> unhandled = new ArrayList<>();
        for (Map.Entry<String, String> entry : flatData.entrySet()) {
            if (!replacedPaths.contains(entry.getKey())) {
                unhandled.add(entry);
            }
        }

        if (!unhandled.isEmpty()) {
            output.add("");
            output.add("# --- auto-generated settings ---");
            for (Map.Entry<String, String> entry : unhandled) {
                String[] parts = entry.getKey().split("\\.");
                int depth = parts.length - 1;
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < depth * 2; i++) sb.append(' ');
                sb.append(parts[parts.length - 1]).append(": ").append(entry.getValue());
                output.add(sb.toString());
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (int i = 0; i < output.size(); i++) {
                if (i > 0) writer.newLine();
                writer.write(output.get(i));
            }
        }
    }

    private static String formatRawValue(String val) {
        if (val == null) return "";
        if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
            return val.substring(1, val.length() - 1);
        }
        return val;
    }

    @SuppressWarnings("unchecked")
    private static void flattenData(Map<String, Object> map, String prefix, Map<String, String> result) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String path = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            if (entry.getValue() instanceof Map) {
                flattenData((Map<String, Object>) entry.getValue(), path, result);
            } else if (entry.getValue() instanceof List) {
                List<?> list = (List<?>) entry.getValue();
                boolean allSimple = true;
                for (Object item : list) {
                    if (item instanceof Map) { allSimple = false; break; }
                }
                if (allSimple && !list.isEmpty()) {
                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < list.size(); i++) {
                        if (i > 0) sb.append(", ");
                        Object item = list.get(i);
                        if (item instanceof String) {
                            sb.append("\"").append(escapeYamlString((String) item)).append("\"");
                        } else {
                            sb.append(item);
                        }
                    }
                    sb.append("]");
                    result.put(path, sb.toString());
                } else {
                    result.put(path, "[]");
                }
            } else if (entry.getValue() != null) {
                String val = String.valueOf(entry.getValue());
                if (entry.getValue() instanceof String) {
                    if (val.contains(":") || val.contains("#") || val.contains("{") || val.contains("[")
                            || val.startsWith(" ") || val.endsWith(" ") || val.isEmpty()) {
                        val = "\"" + escapeYamlString(val) + "\"";
                    }
                }
                result.put(path, val);
            }
        }
    }

    private static String escapeYamlString(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
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
