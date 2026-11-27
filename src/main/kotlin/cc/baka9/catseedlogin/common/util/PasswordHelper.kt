package cc.baka9.catseedlogin.common.util

import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayer

object PasswordHelper {
    @JvmStatic fun updatePassword(source: LoginPlayer, newPassword: String): LoginPlayer {
        val copy = source.copy()
        copy.password = newPassword
        copy.crypt()
        return copy
    }

    @JvmStatic fun registerNewPlayer(name: String, password: String): LoginPlayer = LoginPlayer(name, password).also { it.crypt() }
}
