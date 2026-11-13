package cc.baka9.catseedlogin.bungee;

import cc.baka9.catseedlogin.bungee.config.BungeeConfigManager;
import cc.baka9.catseedlogin.common.command.MessageSink;
import cc.baka9.catseedlogin.common.command.ProxyAdminCommand;
import cc.baka9.catseedlogin.common.command.ProxyAdminContext;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import java.util.List;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.TabExecutor;

public class BungeeCommands extends net.md_5.bungee.api.plugin.Command
    implements TabExecutor, ProxyAdminContext {

  private final BungeeConfigManager configManager;

  public BungeeCommands(
      String name, String permission, BungeeConfigManager configManager, String... aliases) {
    super(name, permission, aliases);
    this.configManager = configManager;
    setPermissionMessage(MessageKey.NO_PERMISSION.get());
  }

  @Override
  public void execute(CommandSender commandSender, String[] args) {
    if (commandSender == null || args == null) {
      return;
    }
    ProxyAdminCommand.execute(this, sink(commandSender), args);
  }

  @Override
  public Iterable<String> onTabComplete(CommandSender commandSender, String[] args) {
    if (commandSender == null || !commandSender.hasPermission(getPermission())) {
      return java.util.Collections.emptyList();
    }
    return ProxyAdminCommand.complete(args);
  }

  private static MessageSink sink(CommandSender sender) {
    return text -> sender.sendMessage(TextComponent.fromLegacyText(text));
  }

  @Override
  public String getPlatformName() {
    return "BungeeCord";
  }

  @Override
  public String getCommandUsage() {
    return "/cslb";
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
    return ProxyServer.getInstance().getServerInfo(getLoginServerName()) != null;
  }

  @Override
  public List<String> getLoggedInPlayers() {
    Listeners listeners = PluginMain.instance.getListeners();
    return listeners == null ? java.util.Collections.emptyList() : listeners.getLoggedInPlayers();
  }

  @Override
  public void logError(String message, Throwable throwable) {
    PluginMain.instance.getLogger().severe(message);
    if (throwable != null) {
      throwable.printStackTrace();
    }
  }
}
