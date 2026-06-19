package cc.baka9.catseedlogin.bukkit;

import java.util.logging.Logger;
import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager;
import cc.baka9.catseedlogin.bukkit.database.SQL;

public class PluginContext {
    private static volatile PluginContext instance;

    private final CatSeedLogin plugin;
    private SQL sql;
    private boolean loadProtocolLib;

    private PluginContext(CatSeedLogin plugin, SQL sql, boolean loadProtocolLib) {
        this.plugin = plugin;
        this.sql = sql;
        this.loadProtocolLib = loadProtocolLib;
    }

    public static void init(CatSeedLogin plugin, SQL sql, boolean loadProtocolLib) {
        instance = new PluginContext(plugin, sql, loadProtocolLib);
    }

    public static PluginContext get() {
        if (instance == null) {
            throw new IllegalStateException("PluginContext has not been initialized");
        }
        return instance;
    }

    public static SQL getSql() {
        return get().sql;
    }

    public static void setSql(SQL sql) {
        get().sql = sql;
        get().plugin.sql = sql;
    }

    public static boolean isLoadProtocolLib() {
        return get().loadProtocolLib;
    }

    public static Logger getLogger() {
        return get().plugin.getLogger();
    }

    public static BukkitConfigManager getConfigManager() {
        return get().plugin.getConfigManager();
    }

    public static CatSeedLogin getPlugin() {
        return get().plugin;
    }
}