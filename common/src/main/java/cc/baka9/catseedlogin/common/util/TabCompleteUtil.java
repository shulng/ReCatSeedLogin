package cc.baka9.catseedlogin.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** TAB 补全通用工具，三个平台共用。 */
public final class TabCompleteUtil {

  private TabCompleteUtil() {}

  /** 按玩家已输入的前缀过滤候选列表（忽略大小写）。 */
  public static List<String> filter(Collection<String> candidates, String prefix) {
    if (candidates == null || candidates.isEmpty()) {
      return Collections.emptyList();
    }
    String input = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
    if (input.isEmpty()) {
      return new ArrayList<>(candidates);
    }
    List<String> result = new ArrayList<>();
    for (String candidate : candidates) {
      if (candidate == null) {
        continue;
      }
      if (candidate.toLowerCase(Locale.ROOT).startsWith(input)) {
        result.add(candidate);
      }
    }
    return result;
  }

  /** 按玩家已输入的前缀过滤候选数组（忽略大小写）。 */
  public static List<String> filter(String[] candidates, String prefix) {
    if (candidates == null || candidates.length == 0) {
      return Collections.emptyList();
    }
    List<String> list = new ArrayList<>(candidates.length);
    Collections.addAll(list, candidates);
    return filter(list, prefix);
  }

  /** 取出当前正在输入的那个参数作为前缀。 */
  public static String lastArg(String[] args) {
    return args == null || args.length == 0 ? "" : args[args.length - 1];
  }
}
