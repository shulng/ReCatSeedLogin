package cc.baka9.catseedlogin.common.config;

import java.util.List;

public interface ConfigurationSection {

    String getPath();

    Configuration getRoot();

    default boolean getBoolean(String key, boolean defaultValue) {
        return getRoot().getBoolean(getFullPath(key), defaultValue);
    }

    default int getInt(String key, int defaultValue) {
        return getRoot().getInt(getFullPath(key), defaultValue);
    }

    default long getLong(String key, long defaultValue) {
        return getRoot().getLong(getFullPath(key), defaultValue);
    }

    default String getString(String key, String defaultValue) {
        return getRoot().getString(getFullPath(key), defaultValue);
    }

    default double getDouble(String key, double defaultValue) {
        return getRoot().getDouble(getFullPath(key), defaultValue);
    }

    default List<String> getStringList(String key) {
        return getRoot().getStringList(getFullPath(key));
    }

    default void set(String key, Object value) {
        getRoot().set(getFullPath(key), value);
    }

    default boolean contains(String key) {
        return getRoot().contains(getFullPath(key));
    }

    default String getFullPath(String key) {
        String path = getPath();
        return path.isEmpty() ? key : path + "." + key;
    }
}
