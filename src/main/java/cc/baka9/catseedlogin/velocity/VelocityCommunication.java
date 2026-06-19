package cc.baka9.catseedlogin.velocity;

import cc.baka9.catseedlogin.common.communication.BaseCommunication;
import cc.baka9.catseedlogin.velocity.config.VelocityConfigManager;
import org.slf4j.Logger;

public class VelocityCommunication extends BaseCommunication {

    private final VelocityConfigManager configManager;
    private final Logger logger;

    public VelocityCommunication(VelocityConfigManager configManager, Logger logger) {
        this.configManager = configManager;
        this.logger = logger;
    }

    @Override
    protected String getProxyHost() {
        return configManager.getProxyHost();
    }

    @Override
    protected int getProxyPort() {
        return configManager.getProxyPort();
    }

    @Override
    protected void logError(String message, Exception e) {
        logger.error(message, e);
    }

    @Override
    protected void logWarning(String message) {
        logger.warn(message);
    }

    @Override
    protected String getAuthKey() {
        return configManager.getAuthKey();
    }
}