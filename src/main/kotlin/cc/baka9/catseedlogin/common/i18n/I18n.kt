package cc.baka9.catseedlogin.common.i18n

import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets
import java.text.MessageFormat
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class I18n(private val dataFolder: File, private val resourceProvider: ResourceProvider) {
    fun interface ResourceProvider { fun getResource(name: String): InputStream? }

    companion object {
        private const val LANGUAGES_FOLDER = "languages"
        @Volatile private var instance: I18n? = null

        @JvmStatic fun getInstance(): I18n? = instance
        @JvmStatic fun tr(key: String): String = instance?.get(key) ?: key
        @JvmStatic fun tr(key: String, vararg args: Any?): String = instance?.get(key, *args) ?: key
    }

    private var currentLocale: Locale = Locale.SIMPLIFIED_CHINESE
    private val messages = ConcurrentHashMap<Locale, MutableMap<String, String>>()
    private val placeholders = ConcurrentHashMap<String, Any>()
    private var colorChar = '&'

    init { instance = this }

    fun setLocale(locale: Locale) { currentLocale = locale; loadMessages(locale) }
    fun setLocale(languageTag: String) = setLocale(Locale.forLanguageTag(languageTag))
    fun getLocale(): Locale = currentLocale

    fun loadMessages(locale: Locale) {
        val localeMessages = HashMap<String, String>()
        val fileName = "${locale.toLanguageTag()}.yml"
        val languagesFolder = File(dataFolder, LANGUAGES_FOLDER)
        val customFile = File(languagesFolder, fileName)
        if (customFile.exists()) loadFromFile(customFile, localeMessages)
        val resourcePath = "$LANGUAGES_FOLDER/$fileName"
        try { resourceProvider.getResource(resourcePath)?.use { loadFromStream(it, localeMessages) } } catch (e: Exception) { e.printStackTrace() }
        if (localeMessages.isEmpty()) {
            try { resourceProvider.getResource("languages/zh-CN.yml")?.use { loadFromStream(it, localeMessages) } } catch (e: Exception) { e.printStackTrace() }
        }
        messages[locale] = localeMessages
    }

    private fun loadYaml(reader: Reader, result: MutableMap<String, String>) {
        try {
            val data = Yaml().load<Any?>(reader)
            if (data is Map<*, *>) flattenMap(result, "", data)
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun loadFromFile(file: File, result: MutableMap<String, String>) {
        try { InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8).use { loadYaml(it, result) } } catch (e: Exception) { e.printStackTrace() }
    }

    private fun loadFromStream(stream: InputStream, result: MutableMap<String, String>) {
        try { InputStreamReader(stream, StandardCharsets.UTF_8).use { loadYaml(it, result) } } catch (e: Exception) { e.printStackTrace() }
    }

    private fun flattenMap(result: MutableMap<String, String>, prefix: String, map: Map<*, *>) {
        for ((rawKey, value) in map) {
            val key = if (prefix.isEmpty()) rawKey.toString() else "$prefix.$rawKey"
            if (value is Map<*, *>) flattenMap(result, key, value)
            else if (value != null) try { result[key] = value.toString() } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun lookup(key: String?): String? {
        if (key == null) return null
        return messages[currentLocale]?.get(key)?.let(::translateColors)
    }

    fun get(key: String): String {
        return try { lookup(key) ?: key } catch (_: Exception) { key }
    }

    fun get(key: String, vararg args: Any?): String {
        return try {
            val message = get(key)
            if (args.isNotEmpty()) MessageFormat.format(message, *args) else message
        } catch (_: Exception) { key }
    }

    fun getOrDefault(key: String, defaultValue: String): String = lookup(key) ?: defaultValue
    fun setPlaceholder(key: String?, value: Any?) { if (key != null && value != null) placeholders[key] = value }
    fun removePlaceholder(key: String?) { if (key != null) placeholders.remove(key) }
    fun clearPlaceholders() = placeholders.clear()
    fun translateColors(message: String): String = message.replace(colorChar, '\u00A7')
    fun setColorChar(colorChar: Char) { this.colorChar = colorChar }
    fun getColorChar(): Char = colorChar
    fun reload() { messages.clear(); loadMessages(currentLocale) }
}
