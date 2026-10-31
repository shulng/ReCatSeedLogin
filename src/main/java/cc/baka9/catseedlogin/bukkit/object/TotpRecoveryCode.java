package cc.baka9.catseedlogin.bukkit.object;

import cc.baka9.catseedlogin.common.util.Crypt;
import java.util.Locale;

/**
 * 一次性恢复码。
 *
 * <p>数据库里只存哈希，明文只在生成时展示一次。「是否已使用」只由 {@code used_at} 是否为 NULL 决定——不额外设 {@code used}
 * 列，两列表达同一件事迟早会不同步。
 *
 * <p>哈希用的是 {@link Crypt} 的 Argon2id 而不是 SHA-256：恢复码只有 8 位、字符集 30， 熵约 2^39。SHA-256
 * 在显卡上每秒能算上百亿次，只拿到数据库的话两分钟内就能枚举完； Argon2 是内存硬、抗 GPU 的，同样条件下基本不可行。
 *
 * <p>恢复码只用于在第二步验证里代替 TOTP，不能用于登录的第一步、重置密码或关闭两步验证。
 */
public class TotpRecoveryCode {

  private final String name;
  private final String codeHash;

  /** 使用时间，NULL 表示尚未使用。 */
  private final Long usedAt;

  public TotpRecoveryCode(String name, String codeHash, Long usedAt) {
    this.name = name;
    this.codeHash = codeHash;
    this.usedAt = usedAt;
  }

  public String getName() {
    return name;
  }

  public String getCodeHash() {
    return codeHash;
  }

  public Long getUsedAt() {
    return usedAt;
  }

  public boolean isUsed() {
    return usedAt != null;
  }

  /** 归一化玩家输入的恢复码：去掉分隔符并统一大写，避免手抄时多打或少打连字符。 */
  public static String normalize(String raw) {
    if (raw == null) {
      return "";
    }
    return raw.replaceAll("[^a-zA-Z0-9]", "").toUpperCase(Locale.ROOT);
  }

  /**
   * 计算恢复码哈希。
   *
   * <p>生成和校验必须走同一个归一化方式，否则盐的拼接方式一旦不一致，旧的恢复码会全部失效。
   */
  public static String hash(String name, String rawCode) {
    return Crypt.encrypt(name, normalize(rawCode));
  }

  /** 校验恢复码。 */
  public static boolean matches(String name, String rawCode, String storedHash) {
    if (storedHash == null || storedHash.isEmpty()) {
      return false;
    }
    return Crypt.match(name, normalize(rawCode), storedHash);
  }
}
