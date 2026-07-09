package cc.baka9.catseedlogin.common.i18n;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Internationalization manager.
 *
 * Key fixes from v2:
 * - Bundled resources loaded FIRST, then user custom files overlay on top
 * - Named placeholder support: {name}, {count} etc.
 * - Removed singleton pattern - instance-based instead
 */
public class I18n {

    private static final String LANGUAGES_FOLDER = "languages";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)\\}");

    private final File dataFolder;
    private final ResourceProvider resourceProvider;
    private final Logger logger;

    private Locale currentLocale = Locale.SIMPLIFIED_CHINESE;
    private final Map<Locale, Map<String, String>> messages = new ConcurrentHashMap<>();
    private char colorChar = '&';

    /**
     * Functional interface for loading resources.
     */
    public interface ResourceProvider {
        InputStream getResourceAsStream(String name);
    }

    public I18n(File dataFolder, ResourceProvider resourceProvider, Logger logger) {
        this.dataFolder = dataFolder;
        this.resourceProvider = resourceProvider;
        this.logger = logger;
    }

    /**
     * Set the current locale and load messages.
     */
    public void setLocale(Locale locale) {
        this.currentLocale = locale;
        loadMessages(locale);
    }

    /**
     * Set locale from language tag string (e.g., "zh_CN").
     */
    public void setLocale(String languageTag) {
        // Convert underscore format to locale
        String[] parts = languageTag.replace("-", "_").split("_");
        if (parts.length == 2) {
            setLocale(new Locale(parts[0], parts[1]));
        } else if (parts.length == 1) {
            setLocale(new Locale(parts[0]));
        }
    }

    public Locale getLocale() {
        return currentLocale;
    }

    /**
     * Load messages for a locale.
     * FIXED: Bundled resources loaded FIRST, user files overlay on top.
     */
    public void loadMessages(Locale locale) {
        Map<String, String> localeMessages = new HashMap<>();

        // Step 1: Load bundled defaults FIRST
        String fileName = localeToFileName(locale);
        loadFromResource(fileName, localeMessages);

        // Step 2: Overlay with user custom file (user customizations win)
        if (dataFolder != null) {
            File langDir = new File(dataFolder, LANGUAGES_FOLDER);
            File userFile = new File(langDir, fileName);
            if (userFile.exists()) {
                loadFromFile(userFile, localeMessages);
            }
        }

        // Step 3: If empty, fall back to zh-CN
        if (localeMessages.isEmpty() && !locale.equals(Locale.SIMPLIFIED_CHINESE)) {
            loadMessages(Locale.SIMPLIFIED_CHINESE);
            return;
        }

        messages.put(locale, localeMessages);
    }

    /**
     * Reload all messages.
     */
    public void reload() {
        messages.clear();
        loadMessages(currentLocale);
    }

    /**
     * Get a message by key with named placeholder substitution.
     *
     * @param key the message key
     * @param placeholders named placeholders as alternating key-value pairs:
     *                     "name", "Steve", "count", "5"
     * @return the formatted message
     */
    public String get(String key, String... placeholders) {
        String template = lookup(key);
        if (placeholders.length == 0) {
            return translateColors(template);
        }
        String result = resolvePlaceholders(template, placeholders);
        return translateColors(result);
    }

    /**
     * Get a message by key (no placeholders).
     */
    public String get(String key) {
        return get(key);
    }

    /**
     * Get a message, returning defaultValue if key not found.
     */
    public String getOrDefault(String key, String defaultValue) {
        Map<String, String> msgs = messages.get(currentLocale);
        if (msgs != null && msgs.containsKey(key)) {
            return translateColors(msgs.get(key));
        }
        return defaultValue;
    }

    private String lookup(String key) {
        Map<String, String> msgs = messages.get(currentLocale);
        if (msgs != null) {
            String value = msgs.get(key);
            if (value != null) return value;
        }
        // Fallback to key itself
        return key;
    }

    /**
     * Resolve named placeholders in template.
     * Input: template "Hello {name}, you have {count} messages"
     *        placeholders ["name", "Steve", "count", "5"]
     */
    private String resolvePlaceholders(String template, String[] placeholders) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            map.put(placeholders[i], placeholders[i + 1]);
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = map.getOrDefault(key, matcher.group(0));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Translate color codes (& -> section sign).
     */
    public String translateColors(String message) {
        if (message == null) return null;
        return message.replace(String.valueOf(colorChar), "\u00A7");
    }

    public void setColorChar(char colorChar) {
        this.colorChar = colorChar;
    }

    // --- Loading helpers ---

    private void loadFromResource(String fileName, Map<String, String> target) {
        if (resourceProvider == null) return;
        InputStream is = resourceProvider.getResourceAsStream(LANGUAGES_FOLDER + "/" + fileName);
        if (is != null) {
            loadFromStream(is, target);
        }
    }

    private void loadFromFile(File file, Map<String, String> target) {
        try (FileInputStream fis = new FileInputStream(file)) {
            loadFromStream(fis, target);
        } catch (IOException e) {
            if (logger != null) {
                logger.warning("Failed to load language file: " + file.getName());
            }
        }
    }

    private void loadFromStream(InputStream stream, Map<String, String> target) {
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(reader);
            if (data != null) {
                flattenMap(target, "", data);
            }
        } catch (IOException e) {
            if (logger != null) {
                logger.warning("Failed to parse language stream");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void flattenMap(Map<String, String> result, String prefix, Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                flattenMap(result, key, (Map<String, Object>) value);
            } else {
                result.put(key, String.valueOf(value));
            }
        }
    }

    private String localeToFileName(Locale locale) {
        String language = locale.getLanguage();
        String country = locale.getCountry();
        if (country != null && !country.isEmpty()) {
            return language + "-" + country + ".yml";
        }
        return language + ".yml";
    }
}
