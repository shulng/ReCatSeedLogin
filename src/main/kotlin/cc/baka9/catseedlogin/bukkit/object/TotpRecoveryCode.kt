package cc.baka9.catseedlogin.bukkit.`object`

import cc.baka9.catseedlogin.common.util.Crypt
import java.util.Locale

class TotpRecoveryCode(val name: String, val codeHash: String, val usedAt: Long?) {
    fun isUsed(): Boolean = usedAt != null
    companion object {
        @JvmStatic fun normalize(raw: String?): String = raw?.replace(Regex("[^a-zA-Z0-9]"), "")?.uppercase(Locale.ROOT) ?: ""
        @JvmStatic fun hash(name: String, rawCode: String?): String = Crypt.encrypt(name, normalize(rawCode))
        @JvmStatic fun matches(name: String, rawCode: String?, storedHash: String?): Boolean =
            !storedHash.isNullOrEmpty() && Crypt.match(name, normalize(rawCode), storedHash)
    }
}
