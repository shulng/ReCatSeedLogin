package cc.baka9.catseedlogin.common.api;

import cc.baka9.catseedlogin.common.i18n.I18n;

public interface PlatformAdapter {

    String getName();

    String getVersion();

    void logInfo(String message);

    void logWarn(String message);

    void logError(String message);

    void logError(String message, Throwable throwable);

    void runAsync(Runnable task);

    void runSync(Runnable task);

    void runAsyncLater(Runnable task, long delayTicks);

    void runSyncLater(Runnable task, long delayTicks);

    void runAsyncTimer(Runnable task, long delayTicks, long periodTicks);

    void runSyncTimer(Runnable task, long delayTicks, long periodTicks);

    I18n getI18n();

    Object getPlatformPlayer(String name);

    boolean isPlayerOnline(String name);

    void kickPlayer(String name, String reason);

    void sendMessage(String playerName, String message);

    void broadcast(String message);
}
