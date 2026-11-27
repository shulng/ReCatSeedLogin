package cc.baka9.catseedlogin.common.config

interface ConfigurationSection {
    fun getPath(): String
    fun getRoot(): Configuration

    fun getBoolean(key: String, defaultValue: Boolean): Boolean = getRoot().getBoolean(getFullPath(key), defaultValue)
    fun getInt(key: String, defaultValue: Int): Int = getRoot().getInt(getFullPath(key), defaultValue)
    fun getLong(key: String, defaultValue: Long): Long = getRoot().getLong(getFullPath(key), defaultValue)
    fun getString(key: String, defaultValue: String): String = getRoot().getString(getFullPath(key), defaultValue)
    fun getDouble(key: String, defaultValue: Double): Double = getRoot().getDouble(getFullPath(key), defaultValue)
    fun getStringList(key: String): List<String> = getRoot().getStringList(getFullPath(key))
    fun set(key: String, value: Any?) = getRoot().set(getFullPath(key), value)
    fun contains(key: String): Boolean = getRoot().contains(getFullPath(key))
    fun getFullPath(key: String): String = if (getPath().isEmpty()) key else "${getPath()}.$key"
}
