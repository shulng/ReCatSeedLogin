package cc.baka9.catseedlogin.bukkit

import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

object Cache {
    @Volatile private var playerHashtable = ConcurrentHashMap<String, LoginPlayer>()
    @JvmField @Volatile var isLoaded: Boolean = false
    private val refreshGeneration = AtomicLong()
    @JvmStatic fun getAllLoginPlayer(): List<LoginPlayer> = ArrayList(playerHashtable.values)
    @JvmStatic fun getIgnoreCase(name: String): LoginPlayer? = playerHashtable[name.lowercase()]
    @JvmStatic fun refreshAll() {
        val generation = refreshGeneration.incrementAndGet()
        isLoaded = false
        CatSeedLogin.instance.runTaskAsync {
            try {
                val newMap = ConcurrentHashMap<String, LoginPlayer>()
                CatSeedLogin.sql.getAll().forEach { newMap[it.name.lowercase()] = it }
                if (generation != refreshGeneration.get()) return@runTaskAsync
                playerHashtable = newMap; CatSeedLogin.instance.logger.info("缓存加载 ${playerHashtable.size} 个数据"); isLoaded = true
            } catch (e: Exception) { CatSeedLogin.instance.logger.warning("数据库错误,无法更新缓存!"); e.printStackTrace() }
        }
    }
    @JvmStatic fun refreshAllSync() {
        val generation = refreshGeneration.incrementAndGet()
        try {
            val newMap = ConcurrentHashMap<String, LoginPlayer>(); CatSeedLogin.sql.getAll().forEach { newMap[it.name.lowercase()] = it }
            if (generation != refreshGeneration.get()) return
            playerHashtable = newMap; CatSeedLogin.instance.logger.info("缓存加载 ${playerHashtable.size} 个数据"); isLoaded = true
        } catch (e: Exception) { CatSeedLogin.instance.logger.warning("数据库错误,无法更新缓存!"); e.printStackTrace() }
    }
    @JvmStatic fun refresh(name: String) {
        val generation = refreshGeneration.incrementAndGet()
        CatSeedLogin.instance.runTaskAsync {
            try {
                val newLp = CatSeedLogin.sql.get(name); val key = name.lowercase()
                if (generation != refreshGeneration.get()) return@runTaskAsync
                if (newLp != null) playerHashtable[key] = newLp else playerHashtable.remove(key)
                CatSeedLogin.instance.logger.info("缓存加载 ${playerHashtable.size} 个数据")
            } catch (e: Exception) { CatSeedLogin.instance.logger.warning("数据库错误,无法更新缓存!"); e.printStackTrace() }
        }
    }
}
