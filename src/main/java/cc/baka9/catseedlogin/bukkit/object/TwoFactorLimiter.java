package cc.baka9.catseedlogin.bukkit.object;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 两步验证的失败限流。
 *
 * <p>三条约定：
 *
 * <ul>
 *   <li><b>key 用 UUID</b>，不用玩家名：改名、大小写差异都绕不过去；
 *   <li><b>退服不清零</b>：否则「输错 4 次 → 退出重进」可以无限重试，限流形同虚设。
 *       只在验证成功或计数窗口超时之后清理；
 *   <li><b>一次调用只计一次失败</b>：TOTP 校验时 t-1/t/t+1 三个时间步任一命中即通过，
 *       调用方按「一次校验 = 一次失败」计数。如果每个时间步各计一次，
 *       恰好卡在步长边界上输对的玩家会被快速推到锁定线。
 * </ul>
 */
public final class TwoFactorLimiter {

  private static final Map<UUID, Attempt> ATTEMPTS = new ConcurrentHashMap<>();

  private TwoFactorLimiter() {}

  /** 一个玩家的失败计数。 */
  private static class Attempt {

    private int failedCount;
    private long lastAttempt;
  }

  /**
   * 记录一次校验失败。
   *
   * @param uuid 玩家 UUID
   * @param maxAttempts 允许的连续失败次数，小于等于 0 表示不限制
   * @param windowMillis 失败计数窗口（毫秒），超时后重新计数
   * @return 是否已经达到上限，达到就应当踢出
   */
  public static boolean recordFailure(UUID uuid, int maxAttempts, long windowMillis) {
    if (uuid == null || maxAttempts <= 0) {
      return false;
    }
    Attempt attempt = ATTEMPTS.computeIfAbsent(uuid, k -> new Attempt());
    long now = System.currentTimeMillis();
    synchronized (attempt) {
      if (attempt.lastAttempt > 0 && now - attempt.lastAttempt > windowMillis) {
        attempt.failedCount = 0;
      }
      attempt.failedCount++;
      attempt.lastAttempt = now;
      return attempt.failedCount >= maxAttempts;
    }
  }

  /** 验证成功，清空该玩家的失败计数。 */
  public static void recordSuccess(UUID uuid) {
    if (uuid == null) {
      return;
    }
    ATTEMPTS.remove(uuid);
  }

  /** 当前连续失败次数。 */
  public static int getFailedCount(UUID uuid) {
    Attempt attempt = ATTEMPTS.get(uuid);
    if (attempt == null) {
      return 0;
    }
    synchronized (attempt) {
      return attempt.failedCount;
    }
  }

  /**
   * 清理超过窗口没有失败的条目。
   *
   * <p>计数超时是「清零」规则的一部分，这个方法负责把已经无意义的条目从内存里移除，
   * 否则条目只增不减。
   */
  public static void sweep(long windowMillis) {
    long now = System.currentTimeMillis();
    ATTEMPTS.entrySet()
        .removeIf(
            entry -> {
              Attempt attempt = entry.getValue();
              synchronized (attempt) {
                return attempt.lastAttempt > 0 && now - attempt.lastAttempt > windowMillis;
              }
            });
  }
}
