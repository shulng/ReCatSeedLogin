package cc.baka9.catseedlogin.bukkit.module;

import cc.baka9.catseedlogin.bukkit.BlindingListeners;
import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.bukkit.Listeners;

public final class ListenerModule implements BukkitModule {
  @Override public void enable(CatSeedLogin plugin) {
    plugin.getServer().getPluginManager().registerEvents(new Listeners(), plugin);
    plugin.getServer().getPluginManager().registerEvents(new BlindingListeners(), plugin);
  }
}
