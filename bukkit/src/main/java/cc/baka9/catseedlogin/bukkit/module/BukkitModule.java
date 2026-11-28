package cc.baka9.catseedlogin.bukkit.module;

import cc.baka9.catseedlogin.bukkit.CatSeedLogin;

/** A lifecycle unit of the Bukkit plugin. */
public interface BukkitModule {
  void enable(CatSeedLogin plugin) throws Exception;

  default void disable(CatSeedLogin plugin) throws Exception {}
}
