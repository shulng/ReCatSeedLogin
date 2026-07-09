package cc.baka9.catseedlogin.velocity;

import cc.baka9.catseedlogin.common.communication.BaseCommunication;

/**
 * Velocity communication client.
 */
public class VelocityCommunication extends BaseCommunication {

    private final PluginMain plugin;

    public VelocityCommunication(PluginMain plugin) {
        this.plugin = plugin;
    }

    public void start() {
        plugin.getLogger().info("Communication client initialized");
    }

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
        plugin.getLogger().error("[Communication] " + message, e);
    }

    @Override
    protected void logWarning(String message) {
        plugin.getLogger().warn("[Communication] " + message);
    }

    @Override
    protected String getAuthKey() {
        return plugin.getConfigLoader().getString("proxy.auth-key", "");
    }
}
