package cc.baka9.catseedlogin.velocity;

import cc.baka9.catseedlogin.api.*;
import cc.baka9.catseedlogin.common.hook.DefaultPasswordPolicy;
import cc.baka9.catseedlogin.common.hook.HookRegistryImpl;

import java.util.UUID;

/**
 * Velocity implementation of CatSeedLoginPlugin.
 */
public class VelocityCatSeedLoginPlugin implements CatSeedLoginPlugin {

    private final PluginMain plugin;
    private final CatSeedEventBus eventBus = new CatSeedEventBus();
    private final HookRegistryImpl hookRegistry = new HookRegistryImpl();

    public VelocityCatSeedLoginPlugin(PluginMain plugin) {
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
        return new VelocityCSLPlayer(name);
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
