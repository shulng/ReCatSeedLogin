package cc.baka9.catseedlogin.common.util;

import cc.baka9.catseedlogin.common.model.LoginPlayer;

/** 密码相关的领域工具：统一处理新玩家注册与密码更新时的加密逻辑。 */
public class PasswordHelper {

  public static LoginPlayer updatePassword(LoginPlayer source, String newPassword) {
    LoginPlayer copy = source.copy();
    copy.setPassword(newPassword);
    copy.crypt();
    return copy;
  }

  public static LoginPlayer registerNewPlayer(String name, String password) {
    LoginPlayer lp = new LoginPlayer(name, password);
    lp.crypt();
    return lp;
  }
}
