package cc.baka9.catseedlogin.bukkit.object;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 两步验证的临时状态。
 *
 * <p>这里只放「还没落库」的中间态：玩家执行 {@code /2fa enable} 后生成密钥，密钥先放在内存里， 等玩家用验证器确认（{@code /2fa
 * verify}）成功才加密写库。确认之前数据库里不会有任何痕迹， 中途放弃也不会留下半配置的数据。
 */
public final class TwoFactorState {

  /** 待确认的绑定：玩家名 -> 生成的 TOTP 密钥（明文，尚未加密）。 */
  private static final Map<String, String> PENDING_BIND = new ConcurrentHashMap<>();

  private TwoFactorState() {}

  /** 记录一个待确认的绑定。 */
  public static void putPendingBind(String name, String secret) {
    if (name == null || secret == null) {
      return;
    }
    PENDING_BIND.put(name, secret);
  }

  /** 取出待确认的绑定，没有则返回 null。 */
  public static String getPendingBind(String name) {
    return name == null ? null : PENDING_BIND.get(name);
  }

  /** 移除待确认的绑定，返回被移除的密钥。 */
  public static String removePendingBind(String name) {
    return name == null ? null : PENDING_BIND.remove(name);
  }

  public static boolean hasPendingBind(String name) {
    return name != null && PENDING_BIND.containsKey(name);
  }
}
