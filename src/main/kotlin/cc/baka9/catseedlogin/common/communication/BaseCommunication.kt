package cc.baka9.catseedlogin.common.communication

import cc.baka9.catseedlogin.common.i18n.MessageKey
import cc.baka9.catseedlogin.common.util.CommunicationAuth
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.Socket
import java.net.InetSocketAddress

abstract class BaseCommunication {
    private companion object { const val SOCKET_TIMEOUT_MS = 5000 }
    protected abstract fun getProxyHost(): String
    protected abstract fun getProxyPort(): Int
    protected abstract fun logError(message: String, e: Exception)
    protected abstract fun logWarning(message: String)
    protected abstract fun getAuthKey(): String

    fun sendConnectRequest(playerName: String): Int {
        try {
            createSocket().use { socket ->
                createWriter(socket).use { writer ->
                    writeLine(writer, "Connect")
                    writeLine(writer, playerName)
                    writer.flush()
                    return socket.getInputStream().read()
                }
            }
        } catch (e: IOException) { logError("Failed to send connect request for player: $playerName", e) }
        return 0
    }

    fun sendKeepLoggedInRequest(playerName: String) {
        try {
            createSocket().use { socket ->
                createWriter(socket).use { writer ->
                    writeLine(writer, "KeepLoggedIn")
                    writeLine(writer, playerName)
                    val time = System.currentTimeMillis().toString()
                    writeLine(writer, time)
                    writeLine(writer, CommunicationAuth.encryption(getAuthKey(), playerName, time))
                    writer.flush()
                }
            }
        } catch (e: IOException) { logError("Failed to send keep logged in request for player: $playerName", e) }
    }

    @Throws(IOException::class)
    protected fun createSocket(): Socket = try {
        Socket().apply {
            connect(InetSocketAddress(getProxyHost(), getProxyPort()), SOCKET_TIMEOUT_MS)
            soTimeout = SOCKET_TIMEOUT_MS
        }
    } catch (e: IOException) {
        logWarning(MessageKey.CHECK_PROXY_CONFIG.get())
        throw IOException(e)
    }

    @Throws(IOException::class)
    protected fun createWriter(socket: Socket): BufferedWriter = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))

    @Throws(IOException::class)
    private fun writeLine(writer: BufferedWriter?, line: String?) {
        if (writer == null || line == null) return
        writer.write(line)
        writer.newLine()
    }
}
