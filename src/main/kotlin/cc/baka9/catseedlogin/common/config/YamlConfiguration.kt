package cc.baka9.catseedlogin.common.config

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Writer
import java.nio.charset.StandardCharsets
import java.util.HashMap

class YamlConfiguration(private val configFile: File?) : Configuration {
    private val yaml = Yaml()
    private val yamlDumper = createDumper()
    private var data: MutableMap<String, Any?> = HashMap()

    companion object {
        private fun createDumper(): Yaml = Yaml(DumperOptions().apply {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            isPrettyFlow = true
            indent = 2
            isAllowUnicode = true
            setSplitLines(true)
            lineBreak = DumperOptions.LineBreak.UNIX
        })

        @JvmStatic fun loadConfiguration(file: File?): YamlConfiguration = YamlConfiguration(file).also {
            if (file != null && file.exists()) try { it.load() } catch (e: IOException) { e.printStackTrace() }
        }
    }

    @Throws(IOException::class)
    fun load() {
        if (configFile == null || !configFile.exists()) return
        InputStreamReader(FileInputStream(configFile), StandardCharsets.UTF_8).use { reader ->
            val loaded = yaml.load<Any?>(reader)
            if (loaded is Map<*, *>) data = loaded.entries.associate { it.key.toString() to it.value }.toMutableMap()
        }
    }

    fun loadFromResource(inputStream: InputStream?) {
        if (inputStream == null) return
        InputStreamReader(inputStream, StandardCharsets.UTF_8).use { reader ->
            val loaded = yaml.load<Any?>(reader)
            if (loaded is Map<*, *>) data = loaded.entries.associate { it.key.toString() to it.value }.toMutableMap()
        }
    }

    @Throws(IOException::class)
    fun save() {
        if (configFile == null) return
        configFile.parentFile?.let { if (!it.exists()) it.mkdirs() }
        OutputStreamWriter(configFile.outputStream(), StandardCharsets.UTF_8).use { writer -> yamlDumper.dump(data, writer) }
    }

    fun getDataMap(): MutableMap<String, Any?> = data

    override fun getBoolean(path: String, defaultValue: Boolean): Boolean = when (val value = get(path)) {
        is Boolean -> value
        is String -> value.toBoolean()
        else -> defaultValue
    }

    private fun <T> parseNumeric(path: String, defaultValue: T, numberFn: (Number) -> T, stringFn: (String) -> T): T {
        return when (val value = getNumeric(path)) {
            is Number -> numberFn(value)
            is String -> try { stringFn(value) } catch (_: NumberFormatException) { defaultValue }
            else -> defaultValue
        }
    }

    private fun getNumeric(path: String): Any? = get(path).let { if (it is Number || it is String) it else null }
    override fun getInt(path: String, defaultValue: Int): Int = parseNumeric(path, defaultValue, Number::toInt, String::toInt)
    override fun getLong(path: String, defaultValue: Long): Long = parseNumeric(path, defaultValue, Number::toLong, String::toLong)
    override fun getDouble(path: String, defaultValue: Double): Double = parseNumeric(path, defaultValue, Number::toDouble, String::toDouble)
    override fun getString(path: String, defaultValue: String): String = get(path)?.toString() ?: defaultValue
    override fun getStringList(path: String): List<String> = (get(path) as? List<*>)?.map { it.toString() } ?: emptyList()
    override fun getSection(path: String): ConfigurationSection = ConfigurationSectionImpl(this, path)
    override fun set(path: String, value: Any?) = setNestedValue(data, path.split("."), 0, value)
    override fun contains(path: String): Boolean = get(path) != null

    fun get(path: String): Any? {
        return try {
            val parts = path.split(".")
            var current: Map<String, Any?> = data
            for (i in 0 until parts.size - 1) {
                val next = current[parts[i]]
                if (next !is Map<*, *>) return null
                @Suppress("UNCHECKED_CAST")
                current = next as Map<String, Any?>
            }
            current[parts.last()]
        } catch (_: Exception) { null }
    }

    private fun setNestedValue(map: MutableMap<String, Any?>, keys: List<String>, index: Int, value: Any?) {
        try {
            if (index == keys.size - 1) {
                if (value == null) map.remove(keys[index]) else map[keys[index]] = value
                return
            }
            var next = map[keys[index]]
            if (next !is MutableMap<*, *>) {
                next = HashMap<String, Any?>()
                map[keys[index]] = next
            }
            @Suppress("UNCHECKED_CAST")
            setNestedValue(next as MutableMap<String, Any?>, keys, index + 1, value)
        } catch (_: Exception) { }
    }

    fun getFile(): File? = configFile

    private class ConfigurationSectionImpl(private val root: Configuration, private val path: String) : ConfigurationSection {
        override fun getPath(): String = path
        override fun getRoot(): Configuration = root
    }
}
