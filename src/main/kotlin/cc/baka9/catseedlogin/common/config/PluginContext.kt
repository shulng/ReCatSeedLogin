package cc.baka9.catseedlogin.common.config

import java.io.File
import java.io.InputStream
import java.util.logging.Logger

interface PluginContext {
    fun getDataFolder(): File
    fun getResourceAsStream(name: String): InputStream?
    fun getLogger(): Logger
}
