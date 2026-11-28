package cc.baka9.catseedlogin.bukkit.module;

import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.bukkit.PluginContext;
import cc.baka9.catseedlogin.bukkit.ProtocolLibListeners;
import cc.baka9.catseedlogin.bukkit.util.TwoFactorMessageUtil;
import cc.baka9.catseedlogin.common.util.SecretCodec;

/** Initializes optional integrations that are independent from commands/listeners. */
public final class IntegrationModule implements BukkitModule {
  private static final String LEGACY_BLINDING_PLUGIN = "CatSeedLoginBlinding";

  @Override
  public void enable(CatSeedLogin plugin) {
    TwoFactorMessageUtil.probe();
    try {
      SecretCodec.init(plugin.getDataFolder());
    } catch (Exception error) {
      plugin.getLogger().warning("初始化两步验证密钥文件失败，两步验证将不可用: " + error.getMessage());
    }

    if (plugin.getServer().getPluginManager().getPlugin(LEGACY_BLINDING_PLUGIN) != null) {
      plugin.getLogger().warning("检测到 " + LEGACY_BLINDING_PLUGIN + " 插件，与内置的登录前失明功能冲突！");
      plugin.getLogger().warning("请删除 " + LEGACY_BLINDING_PLUGIN + " 插件，改用 settings.before-login.blinding 控制该功能");
    }

    if (!plugin.getConfigManager().isEmptyBackpack()) return;
    try {
      Class.forName("com.comphenix.protocol.ProtocolLib");
      CatSeedLogin.loadProtocolLib = true;
      ProtocolLibListeners.enable();
      PluginContext.setLoadProtocolLib(true);
    } catch (ClassNotFoundException error) {
      plugin.getLogger().warning("服务器没有装载ProtocolLib插件，这将无法使用登录前隐藏背包");
    }
  }

  @Override
  public void disable(CatSeedLogin plugin) {
    if (!CatSeedLogin.loadProtocolLib) return;
    try {
      ProtocolLibListeners.disable(plugin);
    } finally {
      CatSeedLogin.loadProtocolLib = false;
      PluginContext.setLoadProtocolLib(false);
    }
  }
}
