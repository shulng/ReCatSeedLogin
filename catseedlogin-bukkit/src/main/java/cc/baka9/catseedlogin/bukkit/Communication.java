package cc.baka9.catseedlogin.bukkit;

import cc.baka9.catseedlogin.common.communication.BaseCommunication;
import cc.baka9.catseedlogin.common.communication.CommunicationProtocol;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * Socket server for proxy communication.
 * Runs on Bukkit side, accepts connections from BungeeCord/Velocity.
 */
public class Communication extends BaseCommunication {

    private final CatSeedLogin plugin;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ServerSocket serverSocket;

    public Communication(CatSeedLogin plugin) {
        this.plugin = plugin;
    }

    /**
     * Start the socket server (async).
     */
    public void startAsync() {
        plugin.getPlatformAdapter().runAsync(this::start);
    }

    /**
     * Start the socket server.
     */
    public void start() {
        if (running.get()) return;

        try {
            serverSocket = new ServerSocket(getProxyPort());
            running.set(true);
            plugin.getLogger().info("Communication server started on port " + getProxyPort());

            while (running.get()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    plugin.getPlatformAdapter().runAsync(() -> handleRequest(clientSocket));
                } catch (IOException e) {
                    if (running.get()) {
                        logError("Error accepting connection", e);
                    }
                }
            }
        } catch (IOException e) {
            logError("Failed to start communication server", e);
        }
    }

    /**
     * Stop the socket server.
     */
    public void stop() {
        running.set(false);
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logError("Error closing server socket", e);
            }
        }
    }

    /**
     * Handle an incoming request.
     */
    private void handleRequest(Socket socket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            OutputStream out = socket.getOutputStream();

            String command = reader.readLine();
            String playerName = reader.readLine();

            if (command == null || playerName == null) {
                return;
            }

            switch (command) {
                case CommunicationProtocol.CMD_CONNECT:
                    handleConnect(playerName, out);
                    break;
                case CommunicationProtocol.CMD_KEEP_LOGGED_IN:
                    handleKeepLoggedIn(reader, playerName);
                    break;
                default:
                    logWarning("Unknown command: " + command);
            }
        } catch (IOException e) {
            logError("Error handling request", e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Handle Connect request.
     */
    private void handleConnect(String playerName, OutputStream out) throws IOException {
        boolean loggedIn = isPlayerLoggedIn(playerName);
        out.write(loggedIn ? CommunicationProtocol.RESPONSE_SUCCESS : CommunicationProtocol.RESPONSE_FAILURE);
        out.flush();
    }

    /**
     * Handle KeepLoggedIn request.
     */
    private void handleKeepLoggedIn(BufferedReader reader, String playerName) throws IOException {
        String time = reader.readLine();
        String hmac = reader.readLine();

        if (time == null || hmac == null) return;

        // Verify HMAC
        String expectedHmac = computeHmac(playerName, time, getAuthKey());
        if (!expectedHmac.equals(hmac)) {
            logWarning("Invalid HMAC for keep-logged-in request from " + playerName);
            return;
        }

        // Add player to logged-in state
        // TODO: Use session manager
    }

    private boolean isPlayerLoggedIn(String playerName) {
        // TODO: Check via session manager
        return false;
    }

    @Override
    protected String getProxyHost() {
        return plugin.getConfigLoader().getString("proxy.host", "127.0.0.1");
    }

    @Override
    protected int getProxyPort() {
        return plugin.getConfigLoader().getInt("proxy.port", 2333);
    }

    @Override
    protected void logError(String message, Exception e) {
        plugin.getLogger().log(Level.SEVERE, "[Communication] " + message, e);
    }

    @Override
    protected void logWarning(String message) {
        plugin.getLogger().warning("[Communication] " + message);
    }

    @Override
    protected String getAuthKey() {
        return plugin.getConfigLoader().getString("proxy.auth-key", "");
    }
}
