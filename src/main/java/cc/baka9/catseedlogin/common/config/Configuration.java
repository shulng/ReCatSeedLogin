package cc.baka9.catseedlogin.common.config;

import java.util.List;

public interface Configuration {

    boolean getBoolean(String path, boolean defaultValue);

    int getInt(String path, int defaultValue);

    long getLong(String path, long defaultValue);

    String getString(String path, String defaultValue);

    double getDouble(String path, double defaultValue);

    List<String> getStringList(String path);

    ConfigurationSection getSection(String path);

    void set(String path, Object value);

    boolean contains(String path);

    default boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    default int getInt(String path) {
        return getInt(path, 0);
    }

    default long getLong(String path) {
        return getLong(path, 0L);
    }

    default String getString(String path) {
        return getString(path, "");
    }

    default double getDouble(String path) {
        return getDouble(path, 0.0);
    }
}
