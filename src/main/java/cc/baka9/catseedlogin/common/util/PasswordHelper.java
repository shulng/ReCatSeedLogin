package cc.baka9.catseedlogin.common.util;

import cc.baka9.catseedlogin.common.model.LoginPlayer;

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