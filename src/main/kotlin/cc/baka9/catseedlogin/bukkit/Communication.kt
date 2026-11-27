package cc.baka9.catseedlogin.bukkit

import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayer
import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayerHelper
import cc.baka9.catseedlogin.common.communication.BaseCommunication
import cc.baka9.catseedlogin.common.util.CommunicationAuth
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object Communication : BaseCommunication() {
    @Volatile
    private var serverSocket: ServerSocket? = null

    @JvmStatic
    fun socketServerStopAsync() {
        CatScheduler.runTaskAsync(Runnable { socketServerStop() })
    }

    @JvmStatic
    fun socketServerStop() {
        val socket = serverSocket
        if (socket != null && !socket.isClosed) {
            try {
                socket.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @JvmStatic
    fun socketServerStartAsync() {
        CatScheduler.runTaskAsync(Runnable { socketServerStart() })
    }

    @JvmStatic
    fun socketServerStart() {
        try {
            ServerSocket(PluginContext.getConfigManager().getProxyPort(), 50).use { socket ->
                serverSocket = socket
                while (!socket.isClosed) acceptAndHandle(socket)
            }
        } catch (e: IOException) {
            PluginContext.getLogger().warning("无法启动Socket服务器: ${e.message}")
            e.printStackTrace()
        } finally {
            serverSocket = null
        }
    }

    private fun acceptAndHandle(server: ServerSocket) {
        try {
            server.accept().use(::handleRequest)
        } catch (e: IOException) {
            if (!server.isClosed) {
                PluginContext.getLogger().warning("Socket连接处理异常: ${e.message}")
            }
        }
    }

    @Throws(IOException::class)
    private fun handleRequest(socket: Socket) {
        BufferedReader(InputStreamReader(socket.getInputStream())).use { reader ->
            socket.getOutputStream().use { output ->
                when (val requestType = reader.readLine() ?: return) {
                    "Connect" -> handleConnectRequest(output, reader.readLine())
                    "KeepLoggedIn" -> handleKeepLoggedInRequest(
                        reader.readLine(),
                        reader.readLine(),
                        reader.readLine()
                    )
                    else -> requestType.length
                }
            }
        }
    }

    private fun handleKeepLoggedInRequest(playerName: String?, time: String?, sign: String?) {
        if (playerName == null || time == null || sign == null) return
        val expectedSign = CommunicationAuth.encryption(
            PluginContext.getConfigManager().getAuthKey(),
            playerName,
            time
        )
        if (sign != expectedSign) return

        CatScheduler.runTask(Runnable {
            val loginPlayer: LoginPlayer = Cache.getIgnoreCase(playerName) ?: return@Runnable
            LoginPlayerHelper.add(loginPlayer)
            val player: Player? = Bukkit.getPlayerExact(playerName)
            player?.updateInventory()
        })
    }

    private fun handleConnectRequest(outputStream: OutputStream, playerName: String?) {
        val result = playerName?.let(LoginPlayerHelper::isLogin) ?: false
        try {
            outputStream.write(if (result) 1 else 0)
            outputStream.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun getProxyHost(): String = PluginContext.getConfigManager().getProxyHost()

    override fun getProxyPort(): Int = PluginContext.getConfigManager().getProxyPort()

    override fun getAuthKey(): String = PluginContext.getConfigManager().getAuthKey()

    override fun logError(message: String, e: Exception) {
        PluginContext.getLogger().severe(message)
        e.printStackTrace()
    }

    override fun logWarning(message: String) {
        PluginContext.getLogger().warning(message)
    }
}
