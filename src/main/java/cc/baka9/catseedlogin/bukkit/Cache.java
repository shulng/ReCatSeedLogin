package cc.baka9.catseedlogin.bukkit;

import cc.baka9.catseedlogin.common.model.LoginPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {
    private static volatile Map<String, LoginPlayer> PLAYER_HASHTABLE = new ConcurrentHashMap<>();
    public static volatile boolean isLoaded = false;

    public static List<LoginPlayer> getAllLoginPlayer() {
        return new ArrayList<>(PLAYER_HASHTABLE.values());
    }

    public static LoginPlayer getIgnoreCase(String name) {
        return PLAYER_HASHTABLE.get(name.toLowerCase());
    }

    public static void refreshAll() {
        isLoaded = false;
        CatSeedLogin.instance.runTaskAsync(() -> {
            try {
                List<LoginPlayer> newCache = CatSeedLogin.sql.getAll();
                ConcurrentHashMap<String, LoginPlayer> newMap = new ConcurrentHashMap<>();
                newCache.forEach(p -> newMap.put(p.getName().toLowerCase(), p));
                PLAYER_HASHTABLE = newMap;
                CatSeedLogin.instance.getLogger().info("缓存加载 " + PLAYER_HASHTABLE.size() + " 个数据");
                isLoaded = true;
            } catch (Exception e) {
                CatSeedLogin.instance.getLogger().warning("数据库错误,无法更新缓存!");
                e.printStackTrace();
            }
        });
    }

    public static void refreshAllSync() {
        try {
            List<LoginPlayer> newCache = CatSeedLogin.sql.getAll();
            ConcurrentHashMap<String, LoginPlayer> newMap = new ConcurrentHashMap<>();
            newCache.forEach(p -> newMap.put(p.getName().toLowerCase(), p));
            PLAYER_HASHTABLE = newMap;
            CatSeedLogin.instance.getLogger().info("缓存加载 " + PLAYER_HASHTABLE.size() + " 个数据");
            isLoaded = true;
        } catch (Exception e) {
            CatSeedLogin.instance.getLogger().warning("数据库错误,无法更新缓存!");
            e.printStackTrace();
        }
    }

    public static void refresh(String name) {
        CatSeedLogin.instance.runTaskAsync(() -> {
            try {
                LoginPlayer newLp = CatSeedLogin.sql.get(name);
                String key = name.toLowerCase();
                if (newLp != null) {
                    PLAYER_HASHTABLE.put(key, newLp);
                } else {
                    PLAYER_HASHTABLE.remove(key);
                }
                CatSeedLogin.instance.getLogger().info("缓存加载 " + PLAYER_HASHTABLE.size() + " 个数据");
            } catch (Exception e) {
                CatSeedLogin.instance.getLogger().warning("数据库错误,无法更新缓存!");
                e.printStackTrace();
            }
        });
    }
}