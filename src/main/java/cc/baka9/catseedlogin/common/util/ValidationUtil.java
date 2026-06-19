package cc.baka9.catseedlogin.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$"
    );

    private static final Pattern IP_ADDRESS_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

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

    public static boolean isValidIpAddress(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        return IP_ADDRESS_PATTERN.matcher(ip).matches();
    }

    public static boolean isLoopbackAddress(String ip) {
        return withInetAddress(ip, InetAddress::isLoopbackAddress);
    }

    public static boolean isPrivateAddress(String ip) {
        return withInetAddress(ip, addr -> addr.isSiteLocalAddress() || addr.isLinkLocalAddress());
    }

    private static boolean withInetAddress(String ip, java.util.function.Predicate<InetAddress> predicate) {
        if (ip == null) {
            return false;
        }
        try {
            InetAddress address = InetAddress.getByName(ip);
            return predicate.test(address);
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public static String sanitizeIpAddress(String ip) {
        if (ip == null) {
            return null;
        }
        return ip.trim().replaceAll("[^0-9a-fA-F.:]", "");
    }

    public static boolean isValidPlayerName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        return name.matches("^\\w+$") && name.length() >= 2 && name.length() <= 16;
    }
}
