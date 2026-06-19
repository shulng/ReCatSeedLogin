package cc.baka9.catseedlogin.bungee;

import cc.baka9.catseedlogin.bungee.config.BungeeConfigManager;
import cc.baka9.catseedlogin.common.communication.BaseCommunication;

public class BungeeCommunication extends BaseCommunication {

    private final BungeeConfigManager configManager;
    private final java.util.logging.Logger logger;

    public BungeeCommunication(BungeeConfigManager configManager, java.util.logging.Logger logger) {
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
        logger.severe(message);
        e.printStackTrace();
    }

    @Override
    protected void logWarning(String message) {
        logger.warning(message);
    }

    @Override
    protected String getAuthKey() {
        return configManager.getAuthKey();
    }
}