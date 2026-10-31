package cc.baka9.catseedlogin.bukkit.object;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 两步验证的临时状态。
 *
 * <p>这里放两类中间态：
 *
 * <ul>
 *   <li><b>待确认的绑定</b>：玩家执行 {@code /2fa enable} 后生成密钥，密钥先放在内存里，
 *       等玩家用验证器确认（{@code /2fa verify}）成功才加密写库。确认之前数据库里不会有任何痕迹，
 *       中途放弃也不会留下半配置的数据。
 *   <li><b>待验证的登录</b>：玩家密码正确但两步验证还没完成。此时玩家仍然是「未登录」，
 *       登录前的所有限制继续生效，直到 TOTP 校验通过。
 * </ul>
 */
public final class TwoFactorState {

  /** 待确认的绑定：玩家名 -> 生成的 TOTP 密钥（明文，尚未加密）。 */
  private static final Map<String, String> PENDING_BIND = new ConcurrentHashMap<>();

  /** 待验证的登录：玩家名 -> 待验证信息。 */
  private static final Map<String, PendingLogin> PENDING_LOGIN = new ConcurrentHashMap<>();

  private TwoFactorState() {}

  /** 密码已通过但两步验证还没完成的玩家。 */
  public static class PendingLogin {

    private final LoginPlayer loginPlayer;
    private final long timestamp;

    PendingLogin(LoginPlayer loginPlayer, long timestamp) {
      this.loginPlayer = loginPlayer;
      this.timestamp = timestamp;
    }

    public LoginPlayer getLoginPlayer() {
      return loginPlayer;
    }

    /** 进入待验证状态的时间戳（毫秒）。 */
    public long getTimestamp() {
      return timestamp;
    }
  }

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

  /** 记录一个待验证登录的玩家。 */
  public static void setPendingLogin(LoginPlayer lp) {
    if (lp == null) {
      return;
    }
    PENDING_LOGIN.put(lp.getName(), new PendingLogin(lp, System.currentTimeMillis()));
  }

  /** 取出待验证登录的信息，没有则返回 null。 */
  public static PendingLogin getPendingLogin(String name) {
    return name == null ? null : PENDING_LOGIN.get(name);
  }

  /** 移除待验证登录的信息。 */
  public static PendingLogin removePendingLogin(String name) {
    return name == null ? null : PENDING_LOGIN.remove(name);
  }

  /** 玩家是否正卡在两步验证上。 */
  public static boolean isAwaitingTwoFactor(String name) {
    return name != null && PENDING_LOGIN.containsKey(name);
  }

  /** 插件卸载时清空，避免残留。 */
  public static void clear() {
    PENDING_BIND.clear();
    PENDING_LOGIN.clear();
  }
}
