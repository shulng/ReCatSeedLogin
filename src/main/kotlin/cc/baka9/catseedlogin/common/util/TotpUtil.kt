package cc.baka9.catseedlogin.common.util

import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.Arrays
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object TotpUtil {
    const val DEFAULT_PERIOD = 30
    const val DEFAULT_DIGITS = 6
    const val DEFAULT_WINDOW = 1
    private const val SECRET_BYTES = 20
    private const val HMAC_ALGORITHM = "HmacSHA1"
    private const val BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
    private val RANDOM = SecureRandom()

    @JvmStatic fun generateSecret(): String = base32Encode(ByteArray(SECRET_BYTES).also(RANDOM::nextBytes))

    @JvmStatic fun buildOtpAuthUri(issuer: String?, accountName: String?, secret: String?): String {
        val safeIssuer = issuer ?: ""
        val safeAccount = accountName ?: ""
        return "otpauth://totp/${urlEncode(safeIssuer)}:${urlEncode(safeAccount)}?secret=${secret ?: ""}&issuer=${urlEncode(safeIssuer)}"
    }

    @JvmStatic fun generateCode(secret: String?, timeMillis: Long): String? = generateCode(secret, timeMillis, DEFAULT_DIGITS, DEFAULT_PERIOD)

    @JvmStatic fun generateCode(secret: String?, timeMillis: Long, digits: Int, period: Int): String? {
        val key = base32Decode(secret) ?: return null
        if (digits <= 0 || period <= 0) return null
        return format(hotp(key, counterAt(timeMillis, period)), digits)
    }

    @JvmStatic fun verify(secret: String?, code: String?, timeMillis: Long): Boolean = verify(secret, code, timeMillis, DEFAULT_DIGITS, DEFAULT_PERIOD, DEFAULT_WINDOW)

    @JvmStatic fun verify(secret: String?, code: String?, timeMillis: Long, digits: Int, period: Int, window: Int): Boolean {
        val expected = code?.trim().orEmpty()
        if (expected.isEmpty() || digits <= 0 || period <= 0) return false
        val key = base32Decode(secret) ?: return false
        val counter = counterAt(timeMillis, period)
        for (offset in -window..window) if (format(hotp(key, counter + offset), digits) == expected) return true
        return false
    }

    private fun counterAt(timeMillis: Long, period: Int): Long = timeMillis / 1000L / period

    private fun hotp(key: ByteArray, counter: Long): Int {
        val hash = hmacSha1(key, toCounterBytes(counter)) ?: return -1
        if (hash.size < 20) return -1
        val offset = hash[hash.size - 1].toInt() and 0x0F
        return ((hash[offset].toInt() and 0x7F) shl 24) or ((hash[offset + 1].toInt() and 0xFF) shl 16) or ((hash[offset + 2].toInt() and 0xFF) shl 8) or (hash[offset + 3].toInt() and 0xFF)
    }

    private fun toCounterBytes(value: Long): ByteArray {
        var counter = value
        return ByteArray(8).also { bytes -> for (i in 7 downTo 0) { bytes[i] = (counter and 0xFF).toByte(); counter = counter ushr 8 } }
    }

    private fun hmacSha1(key: ByteArray, message: ByteArray): ByteArray? = try {
        Mac.getInstance(HMAC_ALGORITHM).apply { init(SecretKeySpec(key, HMAC_ALGORITHM)) }.doFinal(message)
    } catch (_: NoSuchAlgorithmException) { null } catch (_: InvalidKeyException) { null }

    private fun format(value: Int, digits: Int): String {
        if (value < 0) return ""
        var mod = 1
        repeat(digits) { mod *= 10 }
        return (value % mod).toString().padStart(digits, '0')
    }

    private fun base32Encode(bytes: ByteArray): String {
        val result = StringBuilder((bytes.size * 8 + 4) / 5)
        var buffer = 0
        var bitsLeft = 0
        for (byte in bytes) {
            buffer = (buffer shl 8) or (byte.toInt() and 0xFF)
            bitsLeft += 8
            while (bitsLeft >= 5) { result.append(BASE32_CHARS[(buffer ushr (bitsLeft - 5)) and 0x1F]); bitsLeft -= 5 }
        }
        if (bitsLeft > 0) result.append(BASE32_CHARS[(buffer shl (5 - bitsLeft)) and 0x1F])
        return result.toString()
    }

    private fun base32Decode(secret: String?): ByteArray? {
        if (secret == null) return null
        val normalized = secret.replace(Regex("[\\s-]"), "").uppercase(Locale.ROOT).replace(Regex("=+$"), "")
        if (normalized.isEmpty()) return null
        val out = ByteArray(normalized.length * 5 / 8)
        var index = 0
        var buffer = 0
        var bitsLeft = 0
        for (char in normalized) {
            val value = base32Value(char)
            if (value < 0) return null
            buffer = (buffer shl 5) or value
            bitsLeft += 5
            if (bitsLeft >= 8) { out[index++] = (buffer ushr (bitsLeft - 8)).toByte(); bitsLeft -= 8 }
        }
        return if (index == out.size) out else Arrays.copyOf(out, index)
    }

    private fun base32Value(c: Char): Int = when (c) {
        in 'A'..'Z' -> c - 'A'
        in '2'..'7' -> c - '2' + 26
        else -> -1
    }

    private fun urlEncode(value: String): String = buildString(value.toByteArray(StandardCharsets.UTF_8).size) {
        for (byte in value.toByteArray(StandardCharsets.UTF_8)) {
            val unsigned = byte.toInt() and 0xFF
            val c = unsigned.toChar()
            if (c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9' || c == '-' || c == '.' || c == '_' || c == '~') append(c)
            else append('%').append("%02X".format(unsigned))
        }
    }
}
