package cc.baka9.catseedlogin.bukkit.util;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;

/**
 * 用 adventure 发送可点击复制的消息。
 *
 * <p>这个类只会在探测到服务端存在 adventure 之后才被加载，所以直接引用 adventure 类型是安全的。
 * 但为了避免 {@link TwoFactorMessageUtil} 在类验证阶段就把它连带 adventure 一起加载
 * （在 Spigot 上那会直接 NoClassDefFoundError），那边通过反射调用本类。
 */
public final class TwoFactorAdventureSender {

  private TwoFactorAdventureSender() {}

  /** 发送一组消息，copyText 非空的行可点击复制。 */
  public static void send(Player player, List<TwoFactorMessageUtil.MessageLine> lines) {
    for (TwoFactorMessageUtil.MessageLine line : lines) {
      Component component = Component.text(line.getText());
      if (line.getCopyText() != null) {
        component = component.clickEvent(ClickEvent.copyToClipboard(line.getCopyText()));
      }
      if (line.getHoverText() != null) {
        component = component.hoverEvent(HoverEvent.showText(Component.text(line.getHoverText())));
      }
      player.sendMessage(component);
    }
  }
}
