package cc.baka9.catseedlogin.common.api;

import cc.baka9.catseedlogin.common.i18n.I18n;
import java.io.File;
import java.io.InputStream;

/**
 * 平台适配接口：把各平台（Bukkit / BungeeCord / Velocity）的调度、日志、资源访问等能力抽象出来，
 * 供 {@code common} 层复用。
 */
public interface PlatformAdapter {

  /** 平台名称，用于日志与诊断信息。 */
  String getName();

  void logInfo(String message);

  void logWarn(String message);

  void logError(String message);

  void logError(String message, Throwable throwable);

  /** 插件数据目录，配置文件与语言文件存放于此。 */
  File getDataFolder();

  /** 读取插件 jar 内打包的资源，找不到时返回 {@code null}。 */
  InputStream getResource(String name);

  void runAsync(Runnable task);

  void runSync(Runnable task);

  void runAsyncLater(Runnable task, long delayTicks);

  void runSyncLater(Runnable task, long delayTicks);

  void runAsyncTimer(Runnable task, long delayTicks, long periodTicks);

  void runSyncTimer(Runnable task, long delayTicks, long periodTicks);

  /** 当前语言实例，默认取全局单例。 */
  default I18n getI18n() {
    return I18n.getInstance();
  }

  Object getPlatformPlayer(String name);

  boolean isPlayerOnline(String name);

  void kickPlayer(String name, String reason);

  void sendMessage(String playerName, String message);

  void broadcast(String message);
}
