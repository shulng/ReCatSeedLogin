package cc.baka9.catseedlogin.bukkit.`object`

import cc.baka9.catseedlogin.common.util.DateUtil
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

class EmailCode private constructor(
    var name: String,
    var email: String,
    var durability: Long
) {
    enum class Type { Bind, ResetPassword }

    var createTime: Long = System.currentTimeMillis()
    var code: String = DateUtil.generateVerificationCode()

    fun isExpired(): Boolean = DateUtil.isExpired(createTime, durability)
    fun getRemainingTime(): Long = DateUtil.getRemainingTime(createTime, durability)

    companion object {
        private val bindMap = ConcurrentHashMap<String, EmailCode>(10)
        private val resetPasswordMap = ConcurrentHashMap<String, EmailCode>(10)

        @JvmStatic fun create(name: String, email: String, durability: Long, type: Type): EmailCode {
            try {
                val emailCode = EmailCode(name, email, durability)
                clear()
                getMap(type)[name] = emailCode
                return emailCode
            } catch (e: Exception) {
                e.printStackTrace()
                throw RuntimeException("Failed to create email code for $name", e)
            }
        }

        @JvmStatic fun getByName(name: String?, type: Type?): Optional<EmailCode> {
            if (name == null || type == null) return Optional.empty()
            return try {
                clear(); Optional.ofNullable(getMap(type)[name])
            } catch (_: Exception) { Optional.empty() }
        }

        @JvmStatic fun removeByName(name: String?, type: Type?) {
            if (name == null || type == null) return
            try { clear(); getMap(type).remove(name) } catch (e: Exception) { e.printStackTrace() }
        }

        private fun getMap(type: Type) = if (type == Type.Bind) bindMap else resetPasswordMap
        private fun clear() {
            bindMap.entries.removeIf { DateUtil.isExpired(it.value.createTime, it.value.durability) }
            resetPasswordMap.entries.removeIf { DateUtil.isExpired(it.value.createTime, it.value.durability) }
        }
    }
}
