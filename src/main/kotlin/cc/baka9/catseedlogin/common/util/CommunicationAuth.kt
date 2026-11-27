package cc.baka9.catseedlogin.common.util

import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object CommunicationAuth {
    private const val HMAC_SHA256 = "HmacSHA256"
    private val MAC = ThreadLocal.withInitial {
        try { Mac.getInstance(HMAC_SHA256) } catch (e: NoSuchAlgorithmException) { throw RuntimeException(e) }
    }
    private const val HEX_CHARS = "0123456789abcdef"

    @JvmStatic fun encryption(key: String?, vararg data: String): String {
        require(!key.isNullOrEmpty()) { "HMAC key must not be null or empty" }
        val mac = MAC.get()
        try { mac.init(SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), HMAC_SHA256)) }
        catch (e: InvalidKeyException) { throw RuntimeException(e) }
        val signature = mac.doFinal(data.joinToString(":" ).toByteArray(StandardCharsets.UTF_8))
        val hex = CharArray(signature.size * 2)
        signature.forEachIndexed { i, byte ->
            val value = byte.toInt() and 0xff
            hex[i * 2] = HEX_CHARS[value ushr 4]
            hex[i * 2 + 1] = HEX_CHARS[value and 0x0f]
        }
        return String(hex)
    }
}
