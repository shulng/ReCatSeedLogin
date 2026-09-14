package cc.baka9.catseedlogin.bungee.listener;

import cc.baka9.catseedlogin.bungee.PluginMain;
import cc.baka9.catseedlogin.bungee.config.BungeeConfigManager;
import cc.baka9.catseedlogin.bungee.net.BungeeCommunication;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import cc.baka9.catseedlogin.common.proxy.ProxyLoginTracker;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeListeners implements Listener {

  private final ProxyServer proxyServer = ProxyServer.getInstance();
  private final ProxyLoginTracker tracker;
  private final BungeeConfigManager configManager;

  public BungeeListeners(BungeeConfigManager configManager, BungeeCommunication communication) {
    this.configManager = configManager;
    this.tracker = new ProxyLoginTracker(communication);
  }

  @EventHandler
  public void onChat(ChatEvent event) {
    if (!event.isProxyCommand() || !(event.getSender() instanceof ProxiedPlayer)) {
      return;
    }
    ProxiedPlayer player = (ProxiedPlayer) event.getSender();
    String playerName = player.getName();
    if (!tracker.isLoggedIn(playerName)) {
      event.setCancelled(true);
      handleLogin(player, event.getMessage());
    }
  }

  @EventHandler
  public void onServerConnect(ServerConnectEvent event) {
    if (event.isCancelled()) {
      return;
    }
    String loginServerName = configManager.getLoginServerName();
    if (event.getTarget().getName().equals(loginServerName)) {
      return;
    }
    ProxiedPlayer player = event.getPlayer();
    String playerName = player.getName();
    if (tracker.isLoggedIn(playerName)) {
      return;
    }
    try {
      if (!tracker.markLoggedIn(playerName)) {
        event.setTarget(proxyServer.getServerInfo(loginServerName));
      }
    } catch (Exception e) {
      proxyServer.getLogger().severe("Error checking login status for player: " + playerName);
      e.printStackTrace();
    }
  }

  @EventHandler
  public void onServerConnected(ServerConnectedEvent event) {
    String loginServerName = configManager.getLoginServerName();
    if (!event.getServer().getInfo().getName().equals(loginServerName)) {
      return;
    }
    ProxiedPlayer player = event.getPlayer();
    if (tracker.isLoggedIn(player.getName())) {
      PluginMain.runAsync(() -> tracker.sendKeepLoggedInRequest(player.getName()));
    }
  }

  @EventHandler
  public void onPlayerDisconnect(PlayerDisconnectEvent event) {
    tracker.markLoggedOut(event.getPlayer().getName());
  }

  @EventHandler
  public void onPreLogin(PreLoginEvent event) {
    String playerName = event.getConnection().getName();
    try {
      if (tracker.isAlreadyLoggedInElsewhere(playerName)) {
        event.setCancelReason(new TextComponent(MessageKey.ALREADY_LOGGED_IN_PROXY.get()));
        event.setCancelled(true);
      }
    } catch (Exception e) {
      event.setCancelReason(new TextComponent(MessageKey.ERROR_PLEASE_RETRY.get()));
      event.setCancelled(true);
    }
  }

  private void handleLogin(ProxiedPlayer player, String message) {
    String playerName = player.getName();
    PluginMain.runAsync(
        () -> {
          if (tracker.markLoggedIn(playerName)) {
            if (message != null && !message.isEmpty()) {
              proxyServer.getPluginManager().dispatchCommand(player, message.substring(1));
            }
          }
        });
  }
}