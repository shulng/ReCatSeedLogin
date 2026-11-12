package cc.baka9.catseedlogin.bukkit.object;

import cc.baka9.catseedlogin.common.util.DateUtil;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailCode {
  public enum Type {
    Bind,
    ResetPassword
  }

  /** 两种用途各一张表，避免同一玩家的绑定码与重置码互相覆盖。 */
  private static final Map<Type, Map<String, EmailCode>> CODES = new ConcurrentHashMap<>();

  static {
    CODES.put(Type.Bind, new ConcurrentHashMap<>());
    CODES.put(Type.ResetPassword, new ConcurrentHashMap<>());
  }

  private String name, email, code;
  private long durability, createTime;

  private EmailCode(String name, String email, long durability) {
    this.name = name;
    this.email = email;
    this.durability = durability;
    this.createTime = System.currentTimeMillis();
    this.code = DateUtil.generateVerificationCode();
  }

  public static EmailCode create(String name, String email, long durability, Type type) {
    EmailCode emailCode = new EmailCode(name, email, durability);
    clearExpired();
    CODES.get(type).put(name, emailCode);
    return emailCode;
  }

  public static Optional<EmailCode> getByName(String name, Type type) {
    if (name == null || type == null) {
      return Optional.empty();
    }
    clearExpired();
    return Optional.ofNullable(CODES.get(type).get(name));
  }

  public static void removeByName(String name, Type type) {
    if (name == null || type == null) {
      return;
    }
    clearExpired();
    CODES.get(type).remove(name);
  }

  private static void clearExpired() {
    CODES
        .values()
        .forEach(
            map ->
                map.entrySet()
                    .removeIf(
                        next ->
                            DateUtil.isExpired(
                                next.getValue().getCreateTime(), next.getValue().getDurability())));
  }
}
