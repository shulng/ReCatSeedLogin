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

public class I18n {

  private static volatile I18n instance;
  private static final String LANGUAGES_FOLDER = "languages";

  /** 语言文件统一使用 {@code &} 书写，加载时统一转换成 Minecraft 的 {@code §}。 */
  private static final char COLOR_CHAR = '&';

  private final File dataFolder;
  private final ResourceProvider resourceProvider;
  private volatile Map<String, String> messages = new HashMap<>();
  private Locale currentLocale = Locale.SIMPLIFIED_CHINESE;

  public interface ResourceProvider {
    InputStream getResource(String name);
  }

  public I18n(File dataFolder, ResourceProvider resourceProvider) {
    this.dataFolder = dataFolder;
    this.resourceProvider = resourceProvider;
    instance = this;
  }

  public void setLocale(Locale locale) {
    this.currentLocale = locale;
    loadMessages(locale);
  }

  public void setLocale(String languageTag) {
    setLocale(Locale.forLanguageTag(languageTag));
  }

  public void loadMessages(Locale locale) {
    Map<String, String> localeMessages = new HashMap<>();
    String fileName = locale.toLanguageTag() + ".yml";

    // 先铺内置语言，再用数据目录里的自定义文件覆盖，用户改动才能生效
    String resourcePath = LANGUAGES_FOLDER + "/" + fileName;
    try (InputStream defaultStream = resourceProvider.getResource(resourcePath)) {
      if (defaultStream != null) {
        loadFromStream(defaultStream, localeMessages);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    File customFile = new File(new File(dataFolder, LANGUAGES_FOLDER), fileName);
    if (customFile.exists()) {
      loadFromFile(customFile, localeMessages);
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

    messages = localeMessages;
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
      String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
      Object value = entry.getValue();
      if (value instanceof Map) {
        flattenMap(result, key, (Map<String, Object>) value);
      } else if (value != null) {
        result.put(key, String.valueOf(value));
      }
    }
  }

  private String lookup(String key) {
    String value = messages.get(key);
    return value != null ? translateColors(value) : null;
  }

  public String get(String key) {
    if (key == null) return null;
    String message = lookup(key);
    return message != null ? message : key;
  }

  public String get(String key, Object... args) {
    if (key == null) return null;
    String message = get(key);
    if (args == null || args.length == 0) {
      return message;
    }
    try {
      return MessageFormat.format(message, args);
    } catch (Exception e) {
      return message;
    }
  }

  public String translateColors(String message) {
    if (message == null) return null;
    return message.replace(COLOR_CHAR, '\u00A7');
  }

  public static String tr(String key) {
    I18n i18n = instance;
    return i18n != null ? i18n.get(key) : key;
  }

  public static String tr(String key, Object... args) {
    I18n i18n = instance;
    return i18n != null ? i18n.get(key, args) : key;
  }
}
