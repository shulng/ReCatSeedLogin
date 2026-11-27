package cc.baka9.catseedlogin.common.config

interface Configuration {
    fun getBoolean(path: String, defaultValue: Boolean): Boolean
    fun getInt(path: String, defaultValue: Int): Int
    fun getLong(path: String, defaultValue: Long): Long
    fun getString(path: String, defaultValue: String): String
    fun getDouble(path: String, defaultValue: Double): Double
    fun getStringList(path: String): List<String>
    fun getSection(path: String): ConfigurationSection
    fun set(path: String, value: Any?)
    fun contains(path: String): Boolean

    fun getBoolean(path: String): Boolean = getBoolean(path, false)
    fun getInt(path: String): Int = getInt(path, 0)
    fun getLong(path: String): Long = getLong(path, 0L)
    fun getString(path: String): String = getString(path, "")
    fun getDouble(path: String): Double = getDouble(path, 0.0)
}
