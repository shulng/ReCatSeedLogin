package cc.baka9.catseedlogin.bungee.communication;

import cc.baka9.catseedlogin.common.api.ProxyConfig;
import cc.baka9.catseedlogin.common.communication.ProxyCommunicator;
import java.util.logging.Logger;

/** BungeeCord 端通信客户端。 */
public class BungeeCommunication extends ProxyCommunicator {

  private final Logger logger;

  public BungeeCommunication(ProxyConfig config, Logger logger) {
    super(config);
    this.logger = logger;
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
}
