package cc.baka9.catseedlogin.common.util

import de.mkammerer.argon2.Argon2
import de.mkammerer.argon2.Argon2Factory
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object Crypt {
    private val ARGON2: Argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id, 16, 32)
    private const val ARGON2_ITERATIONS = 4
    private const val ARGON2_MEMORY = 65536
    private const val ARGON2_PARALLELISM = 2
    private const val HEX_CHARS = "0123456789abcdef"
    private const val ARGON2_PREFIX = "\$argon2id\$"

    @JvmStatic fun encrypt(name: String, password: String): String = ARGON2.hash(ARGON2_ITERATIONS, ARGON2_MEMORY, ARGON2_PARALLELISM, password.toCharArray())

    @JvmStatic fun match(name: String, password: String, encrypted: String?): Boolean {
        if (encrypted.isNullOrEmpty()) return false
        return try {
            if (encrypted.startsWith(ARGON2_PREFIX)) ARGON2.verify(encrypted, password.toCharArray())
            else legacyMatch(name, password, encrypted)
        } catch (_: Exception) { false }
    }

    @JvmStatic fun isArgon2(storedHash: String?): Boolean = storedHash?.startsWith(ARGON2_PREFIX) == true

    private fun legacyEncrypt(name: String, password: String): String {
        val text = "ÜÄaeut//&/=I " + password + "7421€547" + name + "__+IÄIH§%NK " + password
        val digest = MessageDigest.getInstance("SHA-512")
        digest.update(text.toByteArray(StandardCharsets.UTF_8), 0, text.length)
        return byteArrayToHexString(digest.digest())
    }

    private fun legacyMatch(name: String, password: String, encrypted: String): Boolean = encrypted == legacyEncrypt(name, password)

    @JvmStatic fun byteArrayToHexString(args: ByteArray): String {
        val chars = CharArray(args.size * 2)
        args.forEachIndexed { i, byte ->
            chars[i * 2] = HEX_CHARS[(byte.toInt() shr 4) and 0xF]
            chars[i * 2 + 1] = HEX_CHARS[byte.toInt() and 0xF]
        }
        return String(chars)
    }
}
