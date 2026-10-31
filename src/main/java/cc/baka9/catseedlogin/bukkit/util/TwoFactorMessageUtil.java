package cc.baka9.catseedlogin.bukkit.util;

import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import java.util.List;
import org.bukkit.entity.Player;

/**
 * 两步验证相关消息的发送通路（骨架）。
 *
 * <p>Bukkit 端此前一律用纯 String 发消息，而「点击复制」依赖 adventure——它只有 Paper 才有，
 * Spigot 上不存在。所以这里做两级降级：
 *
 * <ol>
 *   <li>服务端没有 adventure → 所有玩家都发纯文本；
 *   <li>服务端有 adventure 但玩家是基岩版 (Geyser) → 该玩家发纯文本，复制按钮对他没有意义。
 * </ol>
 *
 * <p>两级必须分开写。第一级是「服务端有没有这个能力」，第二级是「这个玩家能不能用这个能力」，
 * 触发条件、影响范围、排查方式都不同；合并成一个 else 会让人分不清某个玩家为什么没拿到可点击的消息。
 *
 * <p>本阶段只有骨架：adventure 可用性在 {@link #probe()} 里探测一次并缓存，
 * 组件发送分支留空并退回纯文本。后续接入时再把 adventure 调用填进 {@code sendComponent}——
 * 这些调用必须全部隔离在本类内部，否则在缺失 adventure 的服务端上会触发 NoClassDefFoundError。
 */
public final class TwoFactorMessageUtil {

  private static final String ADVENTURE_COMPONENT_CLASS = "net.kyori.adventure.text.Component";

  private static volatile boolean adventureAvailable = false;

  private TwoFactorMessageUtil() {}

  /** 在插件启动时探测一次 adventure 是否可用，结果缓存下来，不要每次发消息都探测。 */
  public static void probe() {
    adventureAvailable = detectAdventure();
  }

  public static boolean isAdventureAvailable() {
    return adventureAvailable;
  }

  /** 发送消息，自动在纯文本与可点击组件之间选择。 */
  public static void send(Player player, List<String> lines) {
    if (player == null || lines == null || lines.isEmpty()) {
      return;
    }
    // 第一级：运行环境不支持
    if (!adventureAvailable) {
      sendPlain(player, lines);
      return;
    }
    // 第二级：支持，但当前玩家用不了（基岩版复制按钮无效）
    if (LoginPlayerHelper.isFloodgatePlayer(player)) {
      sendPlain(player, lines);
      return;
    }
    sendComponent(player, lines);
  }

  private static boolean detectAdventure() {
    try {
      Class.forName(ADVENTURE_COMPONENT_CLASS);
      return true;
    } catch (Throwable ignored) {
      return false;
    }
  }

  private static void sendPlain(Player player, List<String> lines) {
    for (String line : lines) {
      if (line != null) {
        player.sendMessage(line);
      }
    }
  }

  /** TODO(接入两步验证时填充): 组装 Component，带 clickEvent(copyToClipboard) 与 hoverEvent。 */
  private static void sendComponent(Player player, List<String> lines) {
    sendPlain(player, lines);
  }
}
