package cc.baka9.catseedlogin.bukkit.util;

import cc.baka9.catseedlogin.bukkit.object.TotpRecoveryCode;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * 生成一次性恢复码。
 *
 * <p>格式为 {@code xxxx-xxxx}，字母表剔除了容易看错的 0/O/1/I/L。8 位、30 个字符，熵约 2^39， 配合 Argon2 哈希足以抵抗离线枚举。
 */
public final class RecoveryCodeGenerator {

  /** 去掉了容易看错的 0、O、1、I、L。 */
  private static final char[] ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();

  private static final int GROUP_LENGTH = 4;
  private static final int GROUP_COUNT = 2;
  private static final SecureRandom RANDOM = new SecureRandom();

  private RecoveryCodeGenerator() {}

  /** 生成结果：明文用于展示，条目用于入库。 */
  public static class Generated {

    private final List<String> rawCodes;
    private final List<TotpRecoveryCode> entries;

    Generated(List<String> rawCodes, List<TotpRecoveryCode> entries) {
      this.rawCodes = rawCodes;
      this.entries = entries;
    }

    /** 恢复码明文，只在展示时用一次。 */
    public List<String> getRawCodes() {
      return rawCodes;
    }

    /** 可入库的条目，只有哈希。 */
    public List<TotpRecoveryCode> getEntries() {
      return entries;
    }
  }

  /**
   * 生成一批恢复码。
   *
   * @param count 生成数量
   */
  public static Generated generate(String name, int count) {
    List<String> rawCodes = new ArrayList<>(count);
    List<TotpRecoveryCode> entries = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      String code = generateRaw();
      rawCodes.add(code);
      entries.add(new TotpRecoveryCode(name, TotpRecoveryCode.hash(name, code), null));
    }
    return new Generated(rawCodes, entries);
  }

  private static String generateRaw() {
    StringBuilder sb = new StringBuilder(GROUP_LENGTH * GROUP_COUNT + GROUP_COUNT - 1);
    for (int group = 0; group < GROUP_COUNT; group++) {
      if (group > 0) {
        sb.append('-');
      }
      for (int i = 0; i < GROUP_LENGTH; i++) {
        sb.append(ALPHABET[RANDOM.nextInt(ALPHABET.length)]);
      }
    }
    return sb.toString();
  }
}
