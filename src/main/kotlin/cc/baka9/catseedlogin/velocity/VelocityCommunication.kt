package cc.baka9.catseedlogin.velocity

import cc.baka9.catseedlogin.common.communication.BaseCommunication
import cc.baka9.catseedlogin.velocity.config.VelocityConfigManager
import org.slf4j.Logger

class VelocityCommunication(
    private val configManager: VelocityConfigManager,
    private val logger: Logger
) : BaseCommunication() {
    override fun getProxyHost(): String = configManager.getProxyHost()

    override fun getProxyPort(): Int = configManager.getProxyPort()

    override fun logError(message: String, e: Exception) {
        logger.error(message, e)
    }

    override fun logWarning(message: String) {
        logger.warn(message)
    }

    override fun getAuthKey(): String = configManager.getAuthKey()
}
