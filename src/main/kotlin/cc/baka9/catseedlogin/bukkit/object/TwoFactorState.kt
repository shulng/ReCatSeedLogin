package cc.baka9.catseedlogin.bukkit.`object`

import java.util.concurrent.ConcurrentHashMap

object TwoFactorState {
    private val pendingBind = ConcurrentHashMap<String, String>()
    private val pendingLogin = ConcurrentHashMap<String, PendingLogin>()
    private val rebindRequired = ConcurrentHashMap<String, Long>()
    class PendingLogin(val loginPlayer: LoginPlayer, val timestamp: Long)

    @JvmStatic fun putPendingBind(name: String?, secret: String?) { if (name != null && secret != null) pendingBind[name] = secret }
    @JvmStatic fun getPendingBind(name: String?): String? = name?.let { pendingBind[it] }
    @JvmStatic fun removePendingBind(name: String?): String? = name?.let { pendingBind.remove(it) }
    @JvmStatic fun hasPendingBind(name: String?): Boolean = name != null && pendingBind.containsKey(name)
    @JvmStatic fun setPendingLogin(lp: LoginPlayer?) { if (lp != null) pendingLogin[lp.name] = PendingLogin(lp, System.currentTimeMillis()) }
    @JvmStatic fun getPendingLogin(name: String?): PendingLogin? = name?.let { pendingLogin[it] }
    @JvmStatic fun removePendingLogin(name: String?): PendingLogin? = name?.let { pendingLogin.remove(it) }
    @JvmStatic fun isAwaitingTwoFactor(name: String?): Boolean = name != null && pendingLogin.containsKey(name)
    @JvmStatic fun setRebindRequired(name: String?) { if (name != null) rebindRequired[name] = System.currentTimeMillis() }
    @JvmStatic fun isRebindRequired(name: String?): Boolean = name != null && rebindRequired.containsKey(name)
    @JvmStatic fun removeRebindRequired(name: String?) { if (name != null) rebindRequired.remove(name) }
    @JvmStatic fun clear() { pendingBind.clear(); pendingLogin.clear(); rebindRequired.clear() }
}
