package cc.baka9.catseedlogin.bukkit.module;

import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.bukkit.Communication;
import cc.baka9.catseedlogin.bukkit.task.Task;

public final class RuntimeModule implements BukkitModule {
  @Override public void enable(CatSeedLogin plugin) {
    if (plugin.getConfigManager().isEnable()) Communication.socketServerStartAsync();
    Task.runAll();
  }
  @Override public void disable(CatSeedLogin plugin) {
    Task.cancelAll();
    Communication.socketServerStop();
  }
}
