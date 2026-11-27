package cc.baka9.catseedlogin.bukkit

import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayerHelper

class CatSeedLoginAPI {
    companion object {
        @JvmStatic fun isLogin(name: String) = LoginPlayerHelper.isLogin(name)
        @JvmStatic fun isRegister(name: String) = LoginPlayerHelper.isRegister(name)
        @JvmStatic fun getLastLoginTime(name: String): Long? = LoginPlayerHelper.getLastLoginTime(name)
    }
}
