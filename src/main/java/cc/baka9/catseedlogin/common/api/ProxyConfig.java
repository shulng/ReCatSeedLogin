package cc.baka9.catseedlogin.common.api;

/** 代理端（BungeeCord / Velocity）与 Bukkit 登录服之间的通信配置。 */
public interface ProxyConfig {

  /** 是否在 Bukkit 端开启 Socket 服务（代理端恒为 false，仅用于读取连接信息）。 */
  boolean isProxyEnabled();

  String getProxyHost();

  int getProxyPort();

  String getAuthKey();

  String getLoginServerName();
}
