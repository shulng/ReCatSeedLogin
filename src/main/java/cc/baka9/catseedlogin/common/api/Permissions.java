package cc.baka9.catseedlogin.common.api;

/** 三个平台共用的权限节点，避免各端各写一份字符串导致漂移。 */
public final class Permissions {

  /** 管理员指令权限，Bukkit / BungeeCord / Velocity 统一使用该节点。 */
  public static final String ADMIN = "catseedlogin.admin";

  private Permissions() {}
}
