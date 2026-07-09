package cc.baka9.catseedlogin.bungee;

import cc.baka9.catseedlogin.common.communication.BaseCommunication;

import java.util.logging.Level;

/**
 * BungeeCord communication client.
 * Connects to Bukkit server's socket server.
 */
public class BungeeCommunication extends BaseCommunication {

    private final PluginMain plugin;

    public BungeeCommunication(PluginMain plugin) {
        this.plugin = plugin;
    }

    /**
     * Start the communication client (connects to Bukkit server).
     */
    public void start() {
        // Communication is request-based, no persistent connection needed
        plugin.getLogger().info("Communication client initialized");
    }

    /**
     * Stop the communication client.
     */
    public void stop() {
        // Nothing to stop for client-side
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
