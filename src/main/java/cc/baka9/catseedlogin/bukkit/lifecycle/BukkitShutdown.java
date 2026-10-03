package cc.baka9.catseedlogin.bukkit.lifecycle;

import cc.baka9.catseedlogin.bukkit.BukkitContext;
import cc.baka9.catseedlogin.bukkit.communication.CommunicationServer;
import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager;
import cc.baka9.catseedlogin.bukkit.database.OfflineLocationStore;
import cc.baka9.catseedlogin.bukkit.session.LoginPlayerHelper;
import cc.baka9.catseedlogin.bukkit.task.Task;
import org.bukkit.Bukkit;

/** 插件卸载时的收尾工作：停止任务、保存在线玩家位置、关闭数据库连接与代理通信。 */
public final class BukkitShutdown {

  private BukkitShutdown() {}

  /**
   * 由主类的 {@code onDisable} 调用。{@code config} 为 {@code null} 表示插件启用阶段就失败，
   * 此时只做能做的清理。
   */
  public static void run(BukkitConfigManager config) {
    Task.cancelAll();
    if (BukkitContext.isInitialized() && config != null) {
      saveOnlinePlayerLocations(config);
      closeDatabase();
    }
    CommunicationServer.stop();
  }

  /** 保存仍在线且已登录玩家的位置，保证重进后能回到退出点。 */
  private static void saveOnlinePlayerLocations(BukkitConfigManager config) {
    Bukkit.getOnlinePlayers()
        .forEach(
            player -> {
              if (LoginPlayerHelper.isLogin(player.getName())
                  && (!player.isDead() || config.isDeathStateQuitRecordLocation())) {
                OfflineLocationStore.saveSync(player);
              }
            });
  }

  private static void closeDatabase() {
    try {
      BukkitContext.getSql().closeConnection();
    } catch (Exception e) {
      BukkitContext.getLogger().warning("关闭数据库连接时出错");
      e.printStackTrace();
    }
  }
}
