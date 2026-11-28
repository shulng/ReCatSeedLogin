package cc.baka9.catseedlogin.bukkit.module;

import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Coordinates independent Bukkit modules and guarantees reverse-order shutdown. */
public final class BukkitModuleManager {
  private final List<BukkitModule> modules = new ArrayList<>();
  private final CatSeedLogin plugin;

  public BukkitModuleManager(CatSeedLogin plugin) {
    this.plugin = plugin;
  }

  public void register(BukkitModule module) {
    modules.add(module);
  }

  public void enableAll() throws Exception {
    int enabled = 0;
    try {
      for (; enabled < modules.size(); enabled++) modules.get(enabled).enable(plugin);
    } catch (Exception error) {
      disableRange(enabled);
      throw error;
    }
  }

  public void disableAll() {
    try {
      disableRange(modules.size());
    } finally {
      modules.clear();
    }
  }

  private void disableRange(int count) {
    List<BukkitModule> enabled = new ArrayList<>(modules.subList(0, count));
    Collections.reverse(enabled);
    for (BukkitModule module : enabled) {
      try {
        module.disable(plugin);
      } catch (Exception error) {
        plugin.getLogger().warning("关闭模块时出错: " + error.getMessage());
      }
    }
  }
}
