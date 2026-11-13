package cc.baka9.catseedlogin.common.command;

import cc.baka9.catseedlogin.common.i18n.MessageKey;
import cc.baka9.catseedlogin.common.util.TabCompleteUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 代理端（BungeeCord / Velocity）管理指令的共用实现。
 *
 * <p>子命令集合、帮助文案、分发与 TAB 补全全部集中在这里，平台侧只负责把
 * {@link ProxyAdminContext} 与 {@link MessageSink} 接上，避免两端功能各长一半。
 */
public final class ProxyAdminCommand {

  /** 所有管理子命令，执行分发与 TAB 补全共用同一份数据源。 */
  public static final List<String> SUB_COMMANDS =
      Collections.unmodifiableList(Arrays.asList("reload", "status", "list", "help"));

  private ProxyAdminCommand() {}

  /** 执行一条管理指令，{@code args} 为去掉指令名后的参数。 */
  public static void execute(ProxyAdminContext context, MessageSink sink, String[] args) {
    if (args == null || args.length == 0) {
      sendHelp(context, sink);
      return;
    }
    try {
      switch (args[0].toLowerCase(Locale.ROOT)) {
        case "reload":
          handleReload(context, sink);
          break;
        case "status":
          handleStatus(context, sink);
          break;
        case "list":
          handleList(context, sink);
          break;
        default:
          sendHelp(context, sink);
          break;
      }
    } catch (Exception e) {
      context.logError("Failed to execute proxy admin command", e);
      sink.send(MessageKey.COMMAND_EXECUTION_ERROR.get(String.valueOf(e.getMessage())));
    }
  }

  /** 返回当前参数下应补全的候选列表。 */
  public static List<String> complete(String[] args) {
    if (args == null || args.length != 1) {
      return Collections.emptyList();
    }
    return TabCompleteUtil.filter(SUB_COMMANDS, args[0]);
  }

  private static void sendHelp(ProxyAdminContext context, MessageSink sink) {
    sink.send(MessageKey.PROXY_HELP_HEADER.get(context.getPlatformName()));
    sink.send(MessageKey.PROXY_HELP_RELOAD.get(context.getCommandUsage()));
    sink.send(MessageKey.PROXY_HELP_STATUS.get(context.getCommandUsage()));
    sink.send(MessageKey.PROXY_HELP_LIST.get(context.getCommandUsage()));
  }

  private static void handleReload(ProxyAdminContext context, MessageSink sink) {
    context.reloadConfigs();
    sink.send(MessageKey.CONFIG_RELOADED.get());
  }

  private static void handleStatus(ProxyAdminContext context, MessageSink sink) {
    sink.send(MessageKey.PROXY_HELP_HEADER.get(context.getPlatformName()));
    sink.send(
        MessageKey.PROXY_STATUS_CONNECTION.get(
            context.getProxyHost(), String.valueOf(context.getProxyPort())));
    sink.send(MessageKey.PROXY_STATUS_LOGIN_SERVER.get(context.getLoginServerName()));
    sink.send(
        context.isLoginServerOnline()
            ? MessageKey.PROXY_STATUS_ONLINE.get()
            : MessageKey.PROXY_STATUS_OFFLINE.get());
  }

  private static void handleList(ProxyAdminContext context, MessageSink sink) {
    List<String> players = context.getLoggedInPlayers();
    sink.send(MessageKey.PROXY_LIST_HEADER.get(String.valueOf(players.size())));
    if (players.isEmpty()) {
      sink.send(MessageKey.PROXY_LIST_EMPTY.get());
      return;
    }
    for (String player : players) {
      sink.send(MessageKey.PROXY_LIST_ITEM.get(player));
    }
  }
}
