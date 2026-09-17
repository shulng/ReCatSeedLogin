package cc.baka9.catseedlogin.velocity.listener;

import cc.baka9.catseedlogin.common.i18n.MessageKey;
import cc.baka9.catseedlogin.common.proxy.ProxyLoginTracker;
import cc.baka9.catseedlogin.velocity.PluginMain;
import cc.baka9.catseedlogin.velocity.config.VelocityConfigManager;
import cc.baka9.catseedlogin.velocity.net.VelocityCommunication;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

public class VelocityListeners {

  private final ProxyLoginTracker tracker;
  private final VelocityConfigManager configManager;
  private final ProxyServer proxyServer;
  private final Logger logger;

  public VelocityListeners(
      VelocityConfigManager configManager,
      VelocityCommunication communication,
      ProxyServer proxyServer,
      Logger logger) {
    this.configManager = configManager;
    this.tracker = new ProxyLoginTracker(communication);
    this.proxyServer = proxyServer;
    this.logger = logger;
  }

  @Subscribe
  public void onChat(com.velocitypowered.api.event.player.PlayerChatEvent event) {
    Player player = event.getPlayer();
    String message = event.getMessage();

    if (message.startsWith("/") && isNotLoggedIn(player)) {
      event.setResult(com.velocitypowered.api.event.player.PlayerChatEvent.ChatResult.denied());
      handleLogin(player, message);
    }
  }

  @Subscribe
  public void onCommandExecute(com.velocitypowered.api.event.command.CommandExecuteEvent event) {
    if (!(event.getCommandSource() instanceof Player)) {
      return;
    }

    Player player = (Player) event.getCommandSource();
    String command = event.getCommand();

    if (isNotLoggedIn(player) && !isCommandAllowed("/" + command)) {
      event.setResult(
          com.velocitypowered.api.event.command.CommandExecuteEvent.CommandResult.denied());
      handleLogin(player, "/" + command);
    }
  }

  private boolean isCommandAllowed(String input) {
    for (java.util.regex.Pattern regex : configManager.getCommandWhiteList()) {
      if (regex.matcher(input).find()) return true;
    }
    return false;
  }

  @Subscribe
  public void onServerPreConnect(ServerPreConnectEvent event) {
    Player player = event.getPlayer();
    RegisteredServer target = event.getResult().getServer().orElse(null);

    if (target == null) return;

    String playerName = player.getUsername();
    String loginServerName = configManager.getLoginServerName();

    if (tracker.isLoggedIn(playerName)) return;

    String targetName = target.getServerInfo().getName();
    if (targetName.equals(loginServerName)) {
      handleLogin(player, null);
      return;
    }

    checkLoginSync(player, playerName, loginServerName, event);
  }

  private void checkLoginSync(
      Player player, String playerName, String loginServerName, ServerPreConnectEvent event) {
    try {
      if (!tracker.markLoggedIn(playerName)) {
        redirectToLoginServer(loginServerName, event);
      }
    } catch (Exception e) {
      logger.error("Error checking login status for player: " + playerName, e);
    }
  }

  private void redirectToLoginServer(String loginServerName, ServerPreConnectEvent event) {
    proxyServer
        .getServer(loginServerName)
        .ifPresent(
            loginServer ->
                event.setResult(ServerPreConnectEvent.ServerResult.allowed(loginServer)));
  }

  @Subscribe
  public void onServerConnected(ServerConnectedEvent event) {
    Player player = event.getPlayer();
    String serverName = event.getServer().getServerInfo().getName();
    String loginServerName = configManager.getLoginServerName();

    if (serverName.equals(loginServerName) && tracker.isLoggedIn(player.getUsername())) {
      PluginMain.runAsyncDelayed(
          () -> {
            tracker.sendKeepLoggedInRequest(player.getUsername());
          },
          1,
          TimeUnit.SECONDS);
    }
  }

  @Subscribe
  public void onPlayerDisconnect(DisconnectEvent event) {
    Player player = event.getPlayer();
    if (player != null) {
      tracker.markLoggedOut(player.getUsername());
    }
  }

  @Subscribe
  public void onPreLogin(PreLoginEvent event) {
    String playerName = event.getUsername();

    try {
      if (tracker.isAlreadyLoggedInElsewhere(playerName)) {
        event.setResult(
            PreLoginEvent.PreLoginComponentResult.denied(
                Component.text(MessageKey.ALREADY_LOGGED_IN_PROXY.get())));
      }
    } catch (Exception e) {
      event.setResult(
          PreLoginEvent.PreLoginComponentResult.denied(
              Component.text(MessageKey.ERROR_PLEASE_RETRY.get())));
    }
  }

  private boolean isNotLoggedIn(Player player) {
    return !tracker.isLoggedIn(player.getUsername());
  }

  private void handleLogin(Player player, String message) {
    String playerName = player.getUsername();
    PluginMain.runAsync(() -> handleLoginAsync(player, playerName, message));
  }

  private void handleLoginAsync(Player player, String playerName, String message) {
    try {
      if (tracker.markLoggedIn(playerName)) {
        executeQueuedCommand(player, message);
      }
    } catch (Exception e) {
      logger.error("Error handling login for player: " + playerName, e);
    }
  }

  private void executeQueuedCommand(Player player, String message) {
    if (message == null || !message.startsWith("/")) return;
    proxyServer.getCommandManager().executeAsync(player, message.substring(1));
  }

  public List<String> getLoggedInPlayers() {
    return tracker.getLoggedInPlayers();
  }
}