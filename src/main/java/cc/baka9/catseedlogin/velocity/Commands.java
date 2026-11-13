package cc.baka9.catseedlogin.velocity;

import cc.baka9.catseedlogin.common.api.Permissions;
import cc.baka9.catseedlogin.common.command.ProxyAdminCommand;
import cc.baka9.catseedlogin.common.command.ProxyAdminContext;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import cc.baka9.catseedlogin.velocity.config.VelocityConfigManager;
import cc.baka9.catseedlogin.velocity.util.VelocityMessages;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;

public class Commands implements SimpleCommand, ProxyAdminContext {

  private final VelocityConfigManager configManager;
  private final ProxyServer proxyServer;
  private final Logger logger;

  public Commands(VelocityConfigManager configManager, ProxyServer proxyServer, Logger logger) {
    this.configManager = configManager;
    this.proxyServer = proxyServer;
    this.logger = logger;
  }

  @Override
  public void execute(Invocation invocation) {
    CommandSource source = invocation.source();
    if (!source.hasPermission(Permissions.ADMIN)) {
      source.sendMessage(VelocityMessages.toComponent(MessageKey.NO_PERMISSION.get()));
      return;
    }
    ProxyAdminCommand.execute(this, VelocityMessages.sink(source), invocation.arguments());
  }

  @Override
  public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
    if (!invocation.source().hasPermission(Permissions.ADMIN)) {
      return CompletableFuture.completedFuture(Collections.emptyList());
    }
    return CompletableFuture.completedFuture(ProxyAdminCommand.complete(invocation.arguments()));
  }

  @Override
  public String getPlatformName() {
    return "Velocity";
  }

  @Override
  public String getCommandUsage() {
    return "/cslv";
  }

  @Override
  public void reloadConfigs() {
    configManager.reload();
  }

  @Override
  public String getProxyHost() {
    return configManager.getProxyHost();
  }

  @Override
  public int getProxyPort() {
    return configManager.getProxyPort();
  }

  @Override
  public String getLoginServerName() {
    return configManager.getLoginServerName();
  }

  @Override
  public boolean isLoginServerOnline() {
    return proxyServer.getServer(getLoginServerName()).isPresent();
  }

  @Override
  public List<String> getLoggedInPlayers() {
    PluginMain plugin = PluginMain.getInstance();
    Listeners listeners = plugin == null ? null : plugin.getListeners();
    return listeners == null ? Collections.emptyList() : listeners.getLoggedInPlayers();
  }

  @Override
  public void logError(String message, Throwable throwable) {
    logger.error(message, throwable);
  }
}
