package cc.baka9.catseedlogin.common.i18n;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code §} 旧式颜色码解析。
 *
 * <p>语言文件统一使用 {@code §} 序列，但各平台渲染方式不同：BungeeCord 有
 * {@code TextComponent.fromLegacyText}，Velocity 只有 adventure。这里把文本拆成
 * 「颜色 + 装饰 + 文本」的段落，让各平台自己映射，避免把序列化器实现差异带进 common。
 */
public final class LegacyText {

  private static final char SECTION = '\u00A7';

  private LegacyText() {}

  /** 一段同一样式的文本。 */
  public static final class Segment {

    private final char color;
    private final String decorations;
    private final String text;

    Segment(char color, String decorations, String text) {
      this.color = color;
      this.decorations = decorations == null ? "" : decorations;
      this.text = text == null ? "" : text;
    }

    /** 颜色码 (0-9a-f)，{@code 0} 表示未指定。 */
    public char getColor() {
      return color;
    }

    /** 装饰码集合，字符取自 k-o，按出现顺序拼接。 */
    public String getDecorations() {
      return decorations;
    }

    public String getText() {
      return text;
    }
  }

  /** 按颜色码与装饰码切分文本，未知的 {@code §x} 原样保留。 */
  public static List<Segment> parse(String input) {
    List<Segment> segments = new ArrayList<>();
    if (input == null || input.isEmpty()) {
      return segments;
    }
    char color = 0;
    StringBuilder decorations = new StringBuilder();
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      char current = input.charAt(i);
      if (current == SECTION && i + 1 < input.length()) {
        char code = Character.toLowerCase(input.charAt(i + 1));
        if (isColor(code)) {
          flush(segments, color, decorations, buffer);
          color = code;
          decorations.setLength(0);
          i++;
          continue;
        }
        if (isDecoration(code)) {
          flush(segments, color, decorations, buffer);
          decorations.append(code);
          i++;
          continue;
        }
        if (code == 'r') {
          flush(segments, color, decorations, buffer);
          color = 0;
          decorations.setLength(0);
          i++;
          continue;
        }
      }
      buffer.append(current);
    }
    flush(segments, color, decorations, buffer);
    return segments;
  }

  private static void flush(
      List<Segment> segments, char color, StringBuilder decorations, StringBuilder buffer) {
    if (buffer.length() == 0) {
      return;
    }
    segments.add(new Segment(color, decorations.toString(), buffer.toString()));
    buffer.setLength(0);
  }

  private static boolean isColor(char code) {
    return (code >= '0' && code <= '9') || (code >= 'a' && code <= 'f');
  }

  private static boolean isDecoration(char code) {
    return code >= 'k' && code <= 'o';
  }
}
