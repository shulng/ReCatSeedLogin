package cc.baka9.catseedlogin.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/** 校验工具：邮箱、密码、IP 地址等输入的合法性判断。 */
public final class ValidationUtil {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile(
          "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

  private static final Pattern PASSWORD_PATTERN =
      Pattern.compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$");

  private ValidationUtil() {}

  public static boolean isValidEmail(String email) {
    if (email == null || email.isEmpty()) {
      return false;
    }
    return EMAIL_PATTERN.matcher(email).matches();
  }

  public static boolean isValidPassword(String password) {
    if (password == null || password.isEmpty()) {
      return false;
    }
    return PASSWORD_PATTERN.matcher(password).matches();
  }

  public static boolean isPasswordTooSimple(String password) {
    return !isValidPassword(password);
  }

  /** 判断 IP 是否为本地回环地址 (127.0.0.1 / ::1 / localhost)。 */
  public static boolean isLoopbackAddress(String ip) {
    return withInetAddress(ip, InetAddress::isLoopbackAddress);
  }

  private static boolean withInetAddress(String ip, Predicate<InetAddress> predicate) {
    if (ip == null) {
      return false;
    }
    try {
      return predicate.test(InetAddress.getByName(ip));
    } catch (UnknownHostException e) {
      return false;
    }
  }
}
