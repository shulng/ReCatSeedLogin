package cc.baka9.catseedlogin.common.api

import java.util.regex.Pattern

interface CoreConfig {
    fun getIpRegisterCountLimit(): Int
    fun getIpCountLimit(): Int
    fun isLimitChineseID(): Boolean
    fun isBedrockLoginBypass(): Boolean
    fun isLoginWithSameIP(): Boolean
    fun isEmptyBackpack(): Boolean
    fun getIPTimeout(): Int
    fun getMaxLengthID(): Int
    fun getMinLengthID(): Int
    fun isBeforeLoginNoDamage(): Boolean
    fun getReenterInterval(): Long
    fun isAfterLoginBack(): Boolean
    fun isCanTpSpawnLocation(): Boolean
    fun getAutoKick(): Int
    fun getNamePattern(): String
    fun isDeathStateQuitRecordLocation(): Boolean
    fun isFloodgatePrefixProtect(): Boolean
    fun isLoopbackLoginBypass(): Boolean
    fun isBeforeLoginAllowChat(): Boolean
    fun isBlindingBeforeLogin(): Boolean
    fun getCommandWhiteList(): List<Pattern>
    fun isTwoFactorEnabled(): Boolean
    fun getTwoFactorIssuer(): String
    fun getTwoFactorDigits(): Int
    fun getTwoFactorPeriod(): Int
    fun getTwoFactorRecoveryCodeCount(): Int
    fun getTwoFactorMaxAttempts(): Int
    fun getTwoFactorAttemptWindow(): Int
    fun getTwoFactorPendingTimeout(): Int

    interface SpawnLocation {
        fun getWorld(): String
        fun getX(): Double
        fun getY(): Double
        fun getZ(): Double
        fun getYaw(): Float
        fun getPitch(): Float
    }

    fun getSpawnLocation(): SpawnLocation
}
