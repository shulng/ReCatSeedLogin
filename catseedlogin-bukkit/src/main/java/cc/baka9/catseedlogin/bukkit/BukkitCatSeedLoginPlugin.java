package cc.baka9.catseedlogin.bukkit;

import cc.baka9.catseedlogin.api.*;
import cc.baka9.catseedlogin.common.hook.DefaultPasswordPolicy;
import cc.baka9.catseedlogin.common.hook.HookRegistryImpl;

import java.util.UUID;

/**
 * Bukkit implementation of the CatSeedLoginPlugin interface.
 */
public class BukkitCatSeedLoginPlugin implements CatSeedLoginPlugin {

    private final CatSeedLogin plugin;
    private final CatSeedEventBus eventBus = new CatSeedEventBus();
    private final HookRegistryImpl hookRegistry = new HookRegistryImpl();

    public BukkitCatSeedLoginPlugin(CatSeedLogin plugin) {
        this.plugin = plugin;
        hookRegistry.registerPasswordPolicy(new DefaultPasswordPolicy());
        hookRegistry.setSessionManager(new BukkitSessionManager(plugin));
    }

    @Override
    public String getVersion() {
        return cc.baka9.catseedlogin.common.Version.VERSION;
    }

    @Override
    public CSLPlayer getPlayer(String name) {
        if (name == null) return null;
        return new BukkitCSLPlayer(plugin, name);
    }

    @Override
    public CSLPlayer getPlayer(UUID uuid) {
        // TODO: Resolve UUID to name
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
