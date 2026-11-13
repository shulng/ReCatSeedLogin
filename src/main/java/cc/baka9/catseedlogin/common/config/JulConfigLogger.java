package cc.baka9.catseedlogin.common.config;

import java.util.logging.Logger;

/** {@link java.util.logging} 平台的配置日志出口（Bukkit / BungeeCord）。 */
public final class JulConfigLogger implements ConfigLogger {

  private final Logger logger;

  public JulConfigLogger(Logger logger) {
    this.logger = logger;
  }

  @Override
  public void info(String message) {
    logger.info(message);
  }

  @Override
  public void warn(String message) {
    logger.warning(message);
  }
}
