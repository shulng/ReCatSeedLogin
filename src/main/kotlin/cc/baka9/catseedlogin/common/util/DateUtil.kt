package cc.baka9.catseedlogin.common.util

import java.util.concurrent.ThreadLocalRandom

object DateUtil {
    private const val VERIFICATION_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    private const val DEFAULT_CODE_LENGTH = 10

    @JvmStatic fun generateVerificationCode(): String = generateVerificationCode(DEFAULT_CODE_LENGTH)

    @JvmStatic fun generateVerificationCode(length: Int): String {
        val actualLength = if (length <= 0) DEFAULT_CODE_LENGTH else length
        val result = StringBuilder(actualLength)
        val random = ThreadLocalRandom.current()
        repeat(actualLength) { result.append(VERIFICATION_CODE_CHARS[random.nextInt(VERIFICATION_CODE_CHARS.length)]) }
        return result.toString()
    }

    @JvmStatic fun isExpired(createTime: Long, durationMillis: Long): Boolean =
        System.currentTimeMillis() - createTime > durationMillis

    @JvmStatic fun getRemainingTime(createTime: Long, durationMillis: Long): Long =
        maxOf(0L, durationMillis - (System.currentTimeMillis() - createTime))
}
