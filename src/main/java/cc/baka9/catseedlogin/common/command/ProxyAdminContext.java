package cc.baka9.catseedlogin.common.command;

import java.util.List;

/** 代理端管理指令需要从平台拿到的信息，让指令逻辑两端共用。 */
public interface ProxyAdminContext {

  /** 平台名称，用于帮助标题。 */
  String getPlatformName();

  /** 指令在帮助里展示的用法名（含前导斜杠，如 {@code /cslv}）。 */
  String getCommandUsage();

  /** 重新加载本端配置。 */
  void reloadConfigs();

  String getProxyHost();

  int getProxyPort();

  String getLoginServerName();

  /** 登录服在代理端是否在线。 */
  boolean isLoginServerOnline();

  /** 代理端内存中已登录的玩家名。 */
  List<String> getLoggedInPlayers();

  /** 指令执行异常时写服务端日志，{@code throwable} 可为 null。 */
  default void logError(String message, Throwable throwable) {}
}
