package cc.baka9.catseedlogin.common.i18n;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class I18n {

  private static volatile I18n instance;
  private static final String LANGUAGES_FOLDER = "languages";

  private final File dataFolder;
  private final ResourceProvider resourceProvider;
  private volatile Locale currentLocale = Locale.SIMPLIFIED_CHINESE;
  private volatile String currentBundleName = Locale.SIMPLIFIED_CHINESE.toLanguageTag();
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
    Locale normalized = normalize(locale);
    this.currentLocale = normalized;
    this.currentBundleName = normalized.toLanguageTag();
    loadMessages(normalized);
  }

  public void setLocale(String languageTag) {
    if (languageTag == null || languageTag.trim().isEmpty()) {
      setLocale(Locale.SIMPLIFIED_CHINESE);
      return;
    }
    String bundleName = languageTag.trim();
    // String configuration is a bundle filename, never a transformed Locale tag.
    this.currentLocale = Locale.ROOT;
    this.currentBundleName = bundleName;
    loadMessages(bundleName);
  }

  public Locale getLocale() {
    return currentLocale;
  }

  public void loadMessages(Locale locale) {
    Locale normalized = normalize(locale);
    loadMessages(normalized.toLanguageTag(), normalized);
  }

  private void loadMessages(String bundleName) {
    loadMessages(bundleName, Locale.ROOT);
  }

  private void loadMessages(String bundleName, Locale locale) {
    Map<String, String> localeMessages = new LinkedHashMap<>();
    File languagesFolder = new File(dataFolder, LANGUAGES_FOLDER);

    if (locale == Locale.ROOT) {
      loadBundle(bundleName, languagesFolder, localeMessages, false);
      loadBundle(bundleName, languagesFolder, localeMessages, true);
    } else {
      // Load the standard Locale fallback chain first; later, more specific bundles override it.
      for (Locale candidate : fallbackLocales(locale)) {
        loadBundle(candidate.toLanguageTag(), languagesFolder, localeMessages, false);
      }
      // Server-local files are overrides and must win over bundled defaults.
      for (Locale candidate : fallbackLocales(locale)) {
        loadBundle(candidate.toLanguageTag(), languagesFolder, localeMessages, true);
      }
    }
    messages.put(locale, localeMessages);
  }

  private void loadBundle(String bundleName, File languagesFolder, Map<String, String> target, boolean custom) {
    String fileName = bundleName + ".yml";
    if (custom) {
      File file = new File(languagesFolder, fileName);
      if (file.exists()) loadFromFile(file, target);
      return;
    }
    try (InputStream stream = resourceProvider.getResource(LANGUAGES_FOLDER + "/" + fileName)) {
      if (stream != null) loadFromStream(stream, target);
    } catch (Exception error) {
      throw new IllegalStateException("Unable to load language bundle " + fileName, error);
    }
  }

  private static Locale normalize(Locale locale) {
    if (locale == null || locale.getLanguage().isEmpty()) return Locale.SIMPLIFIED_CHINESE;
    return Locale.forLanguageTag(locale.toLanguageTag());
  }

  private static Iterable<Locale> fallbackLocales(Locale locale) {
    Map<String, Locale> chain = new LinkedHashMap<>();
    chain.put(Locale.SIMPLIFIED_CHINESE.toLanguageTag(), Locale.SIMPLIFIED_CHINESE);
    if (!locale.getLanguage().isEmpty()) chain.put(locale.getLanguage(), new Locale(locale.getLanguage()));
    if (!locale.getCountry().isEmpty()) chain.put(locale.toLanguageTag(), locale);
    return chain.values();
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
    try (InputStreamReader reader =
        new InputStreamReader(new java.io.FileInputStream(file), StandardCharsets.UTF_8)) {
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
        return new MessageFormat(message, currentLocale).format(args);
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
    if (currentLocale == Locale.ROOT) loadMessages(currentBundleName);
    else loadMessages(currentLocale);
  }

  public static String tr(String key) {
    return instance != null ? instance.get(key) : key;
  }

  public static String tr(String key, Object... args) {
    return instance != null ? instance.get(key, args) : key;
  }
}
