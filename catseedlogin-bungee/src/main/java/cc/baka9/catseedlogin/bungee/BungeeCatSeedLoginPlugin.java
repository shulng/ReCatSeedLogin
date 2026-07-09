package cc.baka9.catseedlogin.bungee;

import cc.baka9.catseedlogin.api.*;
import cc.baka9.catseedlogin.common.hook.DefaultPasswordPolicy;
import cc.baka9.catseedlogin.common.hook.HookRegistryImpl;

import java.util.UUID;

/**
 * BungeeCord implementation of CatSeedLoginPlugin.
 */
public class BungeeCatSeedLoginPlugin implements CatSeedLoginPlugin {

    private final PluginMain plugin;
    private final CatSeedEventBus eventBus = new CatSeedEventBus();
    private final HookRegistryImpl hookRegistry = new HookRegistryImpl();

    public BungeeCatSeedLoginPlugin(PluginMain plugin) {
        this.plugin = plugin;
        hookRegistry.registerPasswordPolicy(new DefaultPasswordPolicy());
        hookRegistry.setSessionManager(plugin.getSessionManager());
    }

    @Override
    public String getVersion() {
        return cc.baka9.catseedlogin.common.Version.VERSION;
    }

    @Override
    public CSLPlayer getPlayer(String name) {
        // BungeeCord doesn't have direct database access, return a basic player
        return new BungeeCSLPlayer(name);
    }

    @Override
    public CSLPlayer getPlayer(UUID uuid) {
        return null;
    }

    @Override
    public CatSeedEventBus getEventBus() {
        return eventBus;
    }

    @Override
    public HookRegistry getHookRegistry() {
        return hookRegistry;
    }
}
