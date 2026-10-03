package cc.baka9.catseedlogin.bukkit.lifecycle;

import cc.baka9.catseedlogin.bukkit.BukkitContext;
import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager;
import cc.baka9.catseedlogin.bukkit.listener.ProtocolLibListeners;
import org.bukkit.Bukkit;

/**
 * 软依赖探测与装载：集中处理 ProtocolLib 与 floodgate 的接入判定。
 *
 * <p>ProtocolLib 的可用性在 {@link #detect} 时确定一次并缓存；floodgate 每次实时查询插件管理器，
 * 与原先在各业务类中直接调用 {@code getPluginManager().getPlugin("floodgate")} 的行为保持一致。
 */
public final class BukkitHooks {

  public static final String PROTOCOL_LIB = "ProtocolLib";
  public static final String FLOODGATE = "floodgate";

  /** ProtocolLib 的主类，用于判定其是否真的可被当前类加载器解析。 */
  private static final String PROTOCOL_LIB_MAIN_CLASS = "com.comphenix.protocol.ProtocolLib";

  private static volatile boolean protocolLibEnabled;

  private BukkitHooks() {}

  /** 在启用流程中调用一次（需在 {@link BukkitContext#init} 之后）：按需装载 ProtocolLib 监听器。 */
  public static void detect(BukkitConfigManager config) {
    protocolLibEnabled = enableProtocolLib(config);
    if (isFloodgateEnabled() && config.isBedrockLoginBypass()) {
      BukkitContext.getLogger().info("检测到floodgate，基岩版兼容已装载");
    }
  }

  /** 登录前隐藏背包功能需要 ProtocolLib；未装载时给出提示并保持功能关闭。 */
  private static boolean enableProtocolLib(BukkitConfigManager config) {
    if (!config.isEmptyBackpack()) {
      return false;
    }
    try {
      Class.forName(PROTOCOL_LIB_MAIN_CLASS);
      ProtocolLibListeners.enable();
      return true;
    } catch (ClassNotFoundException e) {
      BukkitContext.getLogger().warning("服务器没有装载ProtocolLib插件，这将无法使用登录前隐藏背包");
      return false;
    }
  }

  /** ProtocolLib 数据包拦截器是否已成功装载。 */
  public static boolean isProtocolLibEnabled() {
    return protocolLibEnabled;
  }

  /** 服务器是否安装了 floodgate（基岩版兼容）。 */
  public static boolean isFloodgateEnabled() {
    return Bukkit.getPluginManager().getPlugin(FLOODGATE) != null;
  }
}
