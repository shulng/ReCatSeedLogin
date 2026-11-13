package cc.baka9.catseedlogin.velocity.config;

import cc.baka9.catseedlogin.common.config.ConfigLogger;
import cc.baka9.catseedlogin.velocity.PluginMain;

/** 把 Velocity 的 slf4j logger 接到配置管理器的日志出口上。 */
final class VelocityLoggerAdapter implements ConfigLogger {

  private final PluginMain plugin;

  VelocityLoggerAdapter(PluginMain plugin) {
    this.plugin = plugin;
  }

  @Override
  public void info(String message) {
    plugin.getLogger().info(message);
  }

  @Override
  public void warn(String message) {
    plugin.getLogger().warn(message);
  }
}
