package cc.baka9.catseedlogin.bukkit;

import cc.baka9.catseedlogin.bukkit.session.LoginPlayerHelper;

/**
 * 供其他插件调用的公开 API
 *
 * @author handy
 */
public final class CatSeedLoginAPI {

  private CatSeedLoginAPI() {}

  /**
   * 是否登录
   *
   * @param name 玩家名
   * @return true 是
   */
  public static boolean isLogin(String name) {
    return LoginPlayerHelper.isLogin(name);
  }

  /**
   * 是否注册
   *
   * @param name 玩家名
   * @return true 注册
   */
  public static boolean isRegister(String name) {
    return LoginPlayerHelper.isRegister(name);
  }

  /**
   * 获取最后登录时间戳
   *
   * @param name 玩家名
   * @return 时间戳- 未注册为null
   * @since 1.4.2
   */
  public static Long getLastLoginTime(String name) {
    return LoginPlayerHelper.getLastLoginTime(name);
  }
}
