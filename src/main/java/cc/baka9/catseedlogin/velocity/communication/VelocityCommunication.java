package cc.baka9.catseedlogin.velocity.communication;

import cc.baka9.catseedlogin.common.api.ProxyConfig;
import cc.baka9.catseedlogin.common.communication.ProxyCommunicator;
import org.slf4j.Logger;

/** Velocity 端通信客户端。 */
public class VelocityCommunication extends ProxyCommunicator {

  private final Logger logger;

  public VelocityCommunication(ProxyConfig config, Logger logger) {
    super(config);
    this.logger = logger;
  }

  @Override
  protected void logError(String message, Exception e) {
    logger.error(message, e);
  }

  @Override
  protected void logWarning(String message) {
    logger.warn(message);
  }
}
