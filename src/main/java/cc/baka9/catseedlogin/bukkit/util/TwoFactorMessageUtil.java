package cc.baka9.catseedlogin.bukkit.util;

import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import java.util.Collections;
import java.util.List;
import org.bukkit.entity.Player;

/**
 * 两步验证相关消息的发送通路。
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
 * <p>adventure 可用性在 {@link #probe()} 里探测一次并缓存，不要每次发消息都探测。
 */
public final class TwoFactorMessageUtil {

  private static final String ADVENTURE_COMPONENT_CLASS = "net.kyori.adventure.text.Component";
  private static final String ADVENTURE_SENDER_CLASS =
      "cc.baka9.catseedlogin.bukkit.util.TwoFactorAdventureSender";

  private static volatile boolean adventureAvailable = false;

  private TwoFactorMessageUtil() {}

  /** 一条消息。copyText 非空表示整行可点击复制，hoverText 非空表示带悬浮提示。 */
  public static class MessageLine {

    private final String text;
    private final String copyText;
    private final String hoverText;

    public MessageLine(String text) {
      this(text, null, null);
    }

    public MessageLine(String text, String copyText, String hoverText) {
      this.text = text == null ? "" : text;
      this.copyText = copyText;
      this.hoverText = hoverText;
    }

    public String getText() {
      return text;
    }

    public String getCopyText() {
      return copyText;
    }

    public String getHoverText() {
      return hoverText;
    }
  }

  /** 在插件启动时探测一次 adventure 是否可用，结果缓存下来。 */
  public static void probe() {
    adventureAvailable = detectAdventure();
  }

  public static boolean isAdventureAvailable() {
    return adventureAvailable;
  }

  /** 发送一组消息，自动在纯文本与可点击组件之间选择。 */
  public static void send(Player player, List<MessageLine> lines) {
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

  /** 发送一条普通文本消息。 */
  public static void sendText(Player player, String text) {
    send(player, Collections.singletonList(new MessageLine(text)));
  }

  private static boolean detectAdventure() {
    try {
      Class.forName(ADVENTURE_COMPONENT_CLASS);
      return true;
    } catch (Throwable ignored) {
      return false;
    }
  }

  private static void sendPlain(Player player, List<MessageLine> lines) {
    for (MessageLine line : lines) {
      player.sendMessage(line.getText());
    }
  }

  /**
   * 走 adventure 发送可点击组件。
   *
   * <p>这里用反射而不是直接调用 {@link TwoFactorAdventureSender}：直接引用会让类验证阶段就去加载
   * adventure，在没有 adventure 的服务端上等于直接崩掉。
   */
  private static void sendComponent(Player player, List<MessageLine> lines) {
    try {
      Class<?> sender = Class.forName(ADVENTURE_SENDER_CLASS);
      sender.getMethod("send", Player.class, List.class).invoke(null, player, lines);
    } catch (Throwable ignored) {
      sendPlain(player, lines);
    }
  }
}
