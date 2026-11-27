package cc.baka9.catseedlogin.bukkit.util

import cc.baka9.catseedlogin.bukkit.`object`.TotpRecoveryCode
import java.security.SecureRandom

object RecoveryCodeGenerator {
    private val alphabet = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray()
    private const val GROUP_LENGTH = 4
    private const val GROUP_COUNT = 2
    private val random = SecureRandom()

    class Generated(private val codes: List<String>, private val recoveryEntries: List<TotpRecoveryCode>) {
        fun getRawCodes(): List<String> = codes
        fun getEntries(): List<TotpRecoveryCode> = recoveryEntries
    }

    @JvmStatic fun generate(name: String, count: Int): Generated {
        val rawCodes = ArrayList<String>(count)
        val entries = ArrayList<TotpRecoveryCode>(count)
        repeat(count) {
            val code = generateRaw()
            rawCodes.add(code)
            entries.add(TotpRecoveryCode(name, TotpRecoveryCode.hash(name, code), null))
        }
        return Generated(rawCodes, entries)
    }

    private fun generateRaw(): String = buildString(GROUP_LENGTH * GROUP_COUNT + GROUP_COUNT - 1) {
        repeat(GROUP_COUNT) { group ->
            if (group > 0) append('-')
            repeat(GROUP_LENGTH) { append(alphabet[random.nextInt(alphabet.size)]) }
        }
    }
}
