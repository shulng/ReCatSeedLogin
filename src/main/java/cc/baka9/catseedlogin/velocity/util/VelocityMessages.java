package cc.baka9.catseedlogin.velocity.util;

import cc.baka9.catseedlogin.common.command.MessageSink;
import cc.baka9.catseedlogin.common.i18n.LegacyText;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Velocity 端的消息发送。
 *
 * <p>语言文件用的是 {@code §} 旧式颜色码，Velocity 只认 adventure 组件，直接 {@code Component.text}
 * 会把颜色码当普通字符显示。这里把 {@link LegacyText} 解析出的段落翻译成组件。
 */
public final class VelocityMessages {

  private VelocityMessages() {}

  /** 把 {@code §} 文本转换成 adventure 组件。 */
  public static Component toComponent(String legacyText) {
    Component result = Component.empty();
    for (LegacyText.Segment segment : LegacyText.parse(legacyText)) {
      result = result.append(style(Component.text(segment.getText()), segment));
    }
    return result;
  }

  /** 为指令来源创建发送器。 */
  public static MessageSink sink(CommandSource source) {
    return text -> source.sendMessage(toComponent(text));
  }

  private static Component style(Component component, LegacyText.Segment segment) {
    Component result = component;
    NamedTextColor color = color(segment.getColor());
    if (color != null) {
      result = result.color(color);
    }
    for (char decoration : segment.getDecorations().toCharArray()) {
      TextDecoration textDecoration = decoration(decoration);
      if (textDecoration != null) {
        result = result.decorate(textDecoration);
      }
    }
    return result;
  }

  private static NamedTextColor color(char code) {
    switch (code) {
      case '0':
        return NamedTextColor.BLACK;
      case '1':
        return NamedTextColor.DARK_BLUE;
      case '2':
        return NamedTextColor.DARK_GREEN;
      case '3':
        return NamedTextColor.DARK_AQUA;
      case '4':
        return NamedTextColor.DARK_RED;
      case '5':
        return NamedTextColor.DARK_PURPLE;
      case '6':
        return NamedTextColor.GOLD;
      case '7':
        return NamedTextColor.GRAY;
      case '8':
        return NamedTextColor.DARK_GRAY;
      case '9':
        return NamedTextColor.BLUE;
      case 'a':
        return NamedTextColor.GREEN;
      case 'b':
        return NamedTextColor.AQUA;
      case 'c':
        return NamedTextColor.RED;
      case 'd':
        return NamedTextColor.LIGHT_PURPLE;
      case 'e':
        return NamedTextColor.YELLOW;
      case 'f':
        return NamedTextColor.WHITE;
      default:
        return null;
    }
  }

  private static TextDecoration decoration(char code) {
    switch (code) {
      case 'k':
        return TextDecoration.OBFUSCATED;
      case 'l':
        return TextDecoration.BOLD;
      case 'm':
        return TextDecoration.STRIKETHROUGH;
      case 'n':
        return TextDecoration.UNDERLINED;
      case 'o':
        return TextDecoration.ITALIC;
      default:
        return null;
    }
  }
}
