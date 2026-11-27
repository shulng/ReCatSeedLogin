package cc.baka9.catseedlogin.bukkit.`object`

import cc.baka9.catseedlogin.common.util.Crypt
import java.util.Objects

class LoginPlayer(var name: String, var password: String) {
    var email: String? = null
    var ips: String? = null
    var lastAction: Long = 0
    var location: String? = null
    var totpSecret: String? = null
    var totpEnabled: Boolean = false

    fun getIpsList(): MutableList<String> = ips?.split(';')?.toMutableList() ?: mutableListOf()
    fun crypt() { password = Crypt.encrypt(name, password) }
    fun copy(): LoginPlayer = LoginPlayer(name, password).also {
        it.email = email; it.ips = ips; it.lastAction = lastAction; it.location = location
        it.totpSecret = totpSecret; it.totpEnabled = totpEnabled
    }
    override fun equals(other: Any?): Boolean = this === other || (other is LoginPlayer && name == other.name)
    override fun hashCode(): Int = Objects.hash(name)
    override fun toString(): String = "LoginPlayer(name=$name, password=$password, email=$email, ips=$ips, lastAction=$lastAction, location=$location, totpSecret=$totpSecret, totpEnabled=$totpEnabled)"
}
