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
import java.net.InetAddress
import java.util.concurrent.Executors
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object Communication : BaseCommunication() {
    @Volatile
    private var serverSocket: ServerSocket? = null
    private val clientExecutor = Executors.newCachedThreadPool()
    private val recentKeepSignatures = ConcurrentHashMap<String, Long>()
    private const val SOCKET_TIMEOUT_MS = 5000

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
    fun socketServerRestartAsync() {
        CatScheduler.runTaskAsync {
            socketServerStop()
            val deadline = System.currentTimeMillis() + 5000L
            while (serverSocket != null && System.currentTimeMillis() < deadline) {
                Thread.sleep(25L)
            }
            if (serverSocket == null) socketServerStart()
        }
    }

    @JvmStatic
    fun socketServerStart() {
        try {
            val manager = PluginContext.getConfigManager()
            require(manager.getAuthKey().isNotBlank()) { "代理认证密钥不能为空" }
            val bindAddress = InetAddress.getByName(manager.getProxyHost())
            ServerSocket(manager.getProxyPort(), 50, bindAddress).use { socket ->
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
            val client = server.accept()
            clientExecutor.submit {
                client.use {
                    it.soTimeout = SOCKET_TIMEOUT_MS
                    handleRequest(it)
                }
            }
        } catch (e: Exception) {
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
        val requestTime = time.toLongOrNull() ?: return
        val now = System.currentTimeMillis()
        if (kotlin.math.abs(now - requestTime) > 30_000L) return
        val expectedSign = CommunicationAuth.encryption(
            PluginContext.getConfigManager().getAuthKey(),
            playerName,
            time
        )
        if (sign != expectedSign) return
        recentKeepSignatures.entries.removeIf { now - it.value > 30_000L }
        if (recentKeepSignatures.putIfAbsent(sign, now) != null) return

        CatScheduler.runTask(Runnable {
            val loginPlayer: LoginPlayer = Cache.getIgnoreCase(playerName) ?: return@Runnable
            LoginPlayerHelper.add(loginPlayer)
            val player: Player? = Bukkit.getPlayerExact(playerName)
            if (player != null && player.isOnline) CatScheduler.updateInventory(player)
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
