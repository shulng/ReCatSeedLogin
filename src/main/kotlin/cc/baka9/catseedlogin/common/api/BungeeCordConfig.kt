package cc.baka9.catseedlogin.common.api

interface BungeeCordConfig {
    fun isEnable(): Boolean
    fun getProxyHost(): String
    fun getProxyPort(): Int
    fun getAuthKey(): String
    fun getLoginServerName(): String
}
