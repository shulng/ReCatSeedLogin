package cc.baka9.catseedlogin.bukkit.module;

import cc.baka9.catseedlogin.bukkit.BlindingListeners;
import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.bukkit.Config;
import cc.baka9.catseedlogin.bukkit.Listeners;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cc.baka9.catseedlogin.bukkit.object.TwoFactorState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;

public final class ListenerModule implements BukkitModule, Listener {
  private final Listener playerListeners = new Listeners();
  private final Listener blindingListeners = new BlindingListeners();

  @Override
  public void enable(CatSeedLogin plugin) {
    PluginManager pluginManager = plugin.getServer().getPluginManager();
    pluginManager.registerEvents(this, plugin);
    pluginManager.registerEvents(playerListeners, plugin);
    pluginManager.registerEvents(blindingListeners, plugin);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    LoginPlayerHelper.onPlayerQuit(event.getPlayer().getName());
  }

  @Override
  public void disable(CatSeedLogin plugin) {
    HandlerList.unregisterAll(this);
    HandlerList.unregisterAll(playerListeners);
    HandlerList.unregisterAll(blindingListeners);
    TwoFactorState.clear();
    for (Player player : plugin.getServer().getOnlinePlayers()) {
      if (LoginPlayerHelper.isLogin(player.getName())
          && (!player.isDead() || plugin.getConfigManager().isDeathStateQuitRecordLocation())) {
        Config.setOfflineLocationSync(player);
      }
    }
  }
}
