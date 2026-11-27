package cc.baka9.catseedlogin.bungee

import cc.baka9.catseedlogin.bungee.config.BungeeConfigManager
import cc.baka9.catseedlogin.common.communication.BaseCommunication
import java.util.logging.Logger

class BungeeCommunication(
    private val configManager: BungeeConfigManager,
    private val logger: Logger
) : BaseCommunication() {
    override fun getProxyHost(): String = configManager.getProxyHost()

    override fun getProxyPort(): Int = configManager.getProxyPort()

    override fun logError(message: String, e: Exception) {
        logger.severe(message)
        e.printStackTrace()
    }

    override fun logWarning(message: String) {
        logger.warning(message)
    }

    override fun getAuthKey(): String = configManager.getAuthKey()
}
