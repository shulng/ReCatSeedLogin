package cc.baka9.catseedlogin.common.util

import java.net.InetAddress
import java.net.UnknownHostException
import java.util.regex.Pattern

object ValidationUtil {
    private val EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")
    private val PASSWORD_PATTERN = Pattern.compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$")
    private val IP_ADDRESS_PATTERN = Pattern.compile("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")

    @JvmStatic fun isValidEmail(email: String?): Boolean = !email.isNullOrEmpty() && EMAIL_PATTERN.matcher(email).matches()
    @JvmStatic fun isValidPassword(password: String?): Boolean = !password.isNullOrEmpty() && PASSWORD_PATTERN.matcher(password).matches()
    @JvmStatic fun isPasswordTooSimple(password: String?): Boolean = !isValidPassword(password)
    @JvmStatic fun isValidIpAddress(ip: String?): Boolean = !ip.isNullOrEmpty() && IP_ADDRESS_PATTERN.matcher(ip).matches()
    @JvmStatic fun isLoopbackAddress(ip: String?): Boolean = withInetAddress(ip) { it.isLoopbackAddress }
    @JvmStatic fun isPrivateAddress(ip: String?): Boolean = withInetAddress(ip) { it.isSiteLocalAddress || it.isLinkLocalAddress }

    private fun withInetAddress(ip: String?, predicate: (InetAddress) -> Boolean): Boolean {
        if (ip == null) return false
        return try { predicate(InetAddress.getByName(ip)) } catch (_: UnknownHostException) { false }
    }

    @JvmStatic fun sanitizeIpAddress(ip: String?): String? = ip?.trim()?.replace(Regex("[^0-9a-fA-F.:]"), "")
    @JvmStatic fun isValidPlayerName(name: String?): Boolean =
        !name.isNullOrEmpty() && name.matches(Regex("^\\w+$")) && name.length in 2..16
}
