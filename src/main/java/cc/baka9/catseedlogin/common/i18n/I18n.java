package cc.baka9.catseedlogin.common.i18n;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class I18n {

    private static volatile I18n instance;
    private static final String LANGUAGES_FOLDER = "languages";

    private final File dataFolder;
    private final ResourceProvider resourceProvider;
    private Locale currentLocale = Locale.SIMPLIFIED_CHINESE;
    private final Map<Locale, Map<String, String>> messages = new ConcurrentHashMap<>();
    private final Map<String, Object> placeholders = new ConcurrentHashMap<>();
    private char colorChar = '&';

    public interface ResourceProvider {
        InputStream getResource(String name);
    }

    public I18n(File dataFolder, ResourceProvider resourceProvider) {
        this.dataFolder = dataFolder;
        this.resourceProvider = resourceProvider;
        instance = this;
    }

    public static I18n getInstance() {
        return instance;
    }

    public void setLocale(Locale locale) {
        this.currentLocale = locale;
        loadMessages(locale);
    }

    public void setLocale(String languageTag) {
        setLocale(Locale.forLanguageTag(languageTag));
    }

    public Locale getLocale() {
        return currentLocale;
    }

    public void loadMessages(Locale locale) {
        Map<String, String> localeMessages = new HashMap<>();
        String fileName = locale.toLanguageTag() + ".yml";
        File languagesFolder = new File(dataFolder, LANGUAGES_FOLDER);

        File customFile = new File(languagesFolder, fileName);
        if (customFile.exists()) {
            loadFromFile(customFile, localeMessages);
        }

        String resourcePath = LANGUAGES_FOLDER + "/" + fileName;
        try (InputStream defaultStream = resourceProvider.getResource(resourcePath)) {
            if (defaultStream != null) {
                loadFromStream(defaultStream, localeMessages);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (localeMessages.isEmpty()) {
            try (InputStream fallbackStream = resourceProvider.getResource("languages/zh-CN.yml")) {
                if (fallbackStream != null) {
                    loadFromStream(fallbackStream, localeMessages);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            messages.put(locale, localeMessages);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadYaml(Reader reader, Map<String, String> messages) {
        org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = yaml.load(reader);
            if (data != null) {
                flattenMap(messages, "", data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFromFile(File file, Map<String, String> messages) {
        try (InputStreamReader reader = new InputStreamReader(
                new java.io.FileInputStream(file), StandardCharsets.UTF_8)) {
            loadYaml(reader, messages);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFromStream(InputStream stream, Map<String, String> messages) {
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            loadYaml(reader, messages);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void flattenMap(Map<String, String> result, String prefix, Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = buildKey(prefix, entry.getKey());
            Object value = entry.getValue();
            processValue(result, key, value);
        }
    }

    private String buildKey(String prefix, String key) {
        return prefix.isEmpty() ? key : prefix + "." + key;
    }

    @SuppressWarnings("unchecked")
    private void processValue(Map<String, String> result, String key, Object value) {
        if (value instanceof Map) {
            flattenMap(result, key, (Map<String, Object>) value);
            return;
        }
        if (value == null) {
            return;
        }
        try {
            result.put(key, String.valueOf(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String lookup(String key) {
        if (currentLocale == null || key == null) {
            return null;
        }
        Map<String, String> localeMessages = messages.get(currentLocale);
        if (localeMessages != null && localeMessages.containsKey(key)) {
            String value = localeMessages.get(key);
            return value != null ? translateColors(value) : null;
        }
        return null;
    }

    public String get(String key) {
        if (key == null) return null;
        try {
            String message = lookup(key);
            return message != null ? message : key;
        } catch (Exception e) {
            return key;
        }
    }

    public String get(String key, Object... args) {
        if (key == null) return null;
        try {
            String message = get(key);
            if (args != null && args.length > 0) {
                return MessageFormat.format(message, args);
            }
            return message;
        } catch (Exception e) {
            return key;
        }
    }

    public String getOrDefault(String key, String defaultValue) {
        String message = lookup(key);
        return message != null ? message : defaultValue;
    }

    public void setPlaceholder(String key, Object value) {
        if (key == null) return;
        try {
            placeholders.put(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removePlaceholder(String key) {
        if (key == null) return;
        try {
            placeholders.remove(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearPlaceholders() {
        placeholders.clear();
    }

    public String translateColors(String message) {
        if (message == null) return null;
        return message.replace(colorChar, '\u00A7');
    }

    public void setColorChar(char colorChar) {
        this.colorChar = colorChar;
    }

    public char getColorChar() {
        return colorChar;
    }

    public void reload() {
        messages.clear();
        loadMessages(currentLocale);
    }

    public static String tr(String key) {
        return instance != null ? instance.get(key) : key;
    }

    public static String tr(String key, Object... args) {
        return instance != null ? instance.get(key, args) : key;
    }
}
