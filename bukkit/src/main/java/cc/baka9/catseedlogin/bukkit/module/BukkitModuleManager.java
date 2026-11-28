package cc.baka9.catseedlogin.bukkit.module;

import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import java.util.ArrayList;
import java.util.List;

/** Coordinates independent Bukkit modules and guarantees reverse-order shutdown. */
public final class BukkitModuleManager {
  private final List<BukkitModule> modules = new ArrayList<>();
  private final List<BukkitModule> startedModules = new ArrayList<>();
  private final CatSeedLogin plugin;

  public BukkitModuleManager(CatSeedLogin plugin) {
    this.plugin = plugin;
  }

  public void register(BukkitModule module) {
    modules.add(module);
  }

  public void enableAll() throws Exception {
    try {
      for (BukkitModule module : modules) {
        startedModules.add(module);
        module.enable(plugin);
      }
    } catch (Exception | LinkageError error) {
      disableAll();
      throw error;
    }
  }

  public void disableAll() {
    while (!startedModules.isEmpty()) {
      BukkitModule module = startedModules.remove(startedModules.size() - 1);
      try {
        module.disable(plugin);
      } catch (Exception | LinkageError error) {
        plugin.getLogger().log(java.util.logging.Level.WARNING, "关闭模块时出错", error);
      }
    }
  }
}
