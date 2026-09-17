package cc.baka9.catseedlogin.bukkit.object;

import cc.baka9.catseedlogin.bukkit.cache.PlayerCache;
import cc.baka9.catseedlogin.bukkit.config.Config;
import cc.baka9.catseedlogin.bukkit.platform.BukkitContext;
import cc.baka9.catseedlogin.bukkit.scheduler.CatScheduler;
import cc.baka9.catseedlogin.common.model.LoginPlayer;
import cc.baka9.catseedlogin.common.util.PasswordHelper;
import cc.baka9.catseedlogin.common.util.ValidationUtil;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.geysermc.floodgate.api.FloodgateApi;

public class LoginPlayerHelper {
  private static final Map<String, LoginPlayer> loginPlayers = new ConcurrentHashMap<>();
  private static final Map<String, Long> playerExitTimes = new ConcurrentHashMap<>();
  private static final int MAX_STORED_IPS = 10;

  public static List<LoginPlayer> getList() {
    return new ArrayList<>(loginPlayers.values());
  }

  public static void add(LoginPlayer lp) {
    if (lp == null) return;
    try {
      loginPlayers.put(lp.getName(), lp);
    } catch (Exception e) {
      BukkitContext.getLogger().severe("Failed to add LoginPlayer: " + e.getMessage());
    }
  }

  public static void remove(LoginPlayer lp) {
    if (lp == null) return;
    try {
      loginPlayers.remove(lp.getName());
    } catch (Exception e) {
      BukkitContext.getLogger().severe("Failed to remove LoginPlayer: " + e.getMessage());
    }
  }

  public static void remove(String name) {
    if (name == null) return;
    try {
      loginPlayers.remove(name);
    } catch (Exception e) {
      BukkitContext.getLogger()
          .severe("Failed to remove LoginPlayer by name: " + name + " - " + e.getMessage());
    }
  }

  public static boolean isLogin(String name) {
    return canBypassLogin(name) || loginPlayers.containsKey(name);
  }

  /** 基岩版(Floodgate)玩家是否启用登录跳过。 */
  public static boolean isBedrockLoginBypassed(Player player) {
    return Config.Settings.bedrockLoginBypass && isFloodgatePlayer(player);
  }

  /**
   * 更新口令并持久化：升哈希、写库并刷新缓存。
   *
   * @return 更新后的 LoginPlayer 副本
   */
  public static LoginPlayer changePasswordAndPersist(LoginPlayer lp, String rawPassword)
      throws Exception {
    LoginPlayer copy = PasswordHelper.updatePassword(lp, rawPassword);
    BukkitContext.getSql().edit(copy);
    PlayerCache.refresh(copy.getName());
    return copy;
  }

  private static boolean canBypassLogin(String name) {
    return (Config.Settings.bedrockLoginBypass && isFloodgatePlayer(name))
        || (Config.Settings.loginWithSameIp && recordCurrentIP(name));
  }

  public static boolean isRegister(String name) {
    return (Config.Settings.bedrockLoginBypass && isFloodgatePlayer(name))
        || PlayerCache.getIgnoreCase(name) != null;
  }

  public static boolean recordCurrentIP(String name) {
    Player player = Bukkit.getPlayerExact(name);
    return player != null && recordCurrentIP(player);
  }

  public static boolean recordCurrentIP(Player player) {
    String currentIP = getPlayerIP(player);
    if (currentIP == null) return false;

    LoginPlayer storedPlayer = PlayerCache.getIgnoreCase(player.getName());
    if (storedPlayer != null) {
      List<String> storedIPs = getStoredIPs(storedPlayer);
      Long exitTime = playerExitTimes.get(player.getName());

      if (ValidationUtil.isLoopbackAddress(currentIP)) return false;
      return Config.Settings.ipTimeout == 0
          ? storedIPs.contains(currentIP)
          : exitTime != null
              && storedIPs.contains(currentIP)
              && (System.currentTimeMillis() - exitTime)
                  <= (long) Config.Settings.ipTimeout * 60 * 1000;
    }

    return false;
  }

  private static String getPlayerIP(Player player) {
    if (player == null) return null;
    return Optional.ofNullable(player.getAddress())
        .map(addr -> addr.getAddress())
        .map(InetAddress::getHostAddress)
        .orElse(null);
  }

  public static void recordPlayerExitTime(String playerName) {
    if (playerName == null) return;
    if (Config.Settings.ipTimeout != 0 && isLogin(playerName)) {
      try {
        playerExitTimes.put(playerName, System.currentTimeMillis());
      } catch (Exception e) {
        BukkitContext.getLogger()
            .severe("Failed to record player exit time: " + playerName + " - " + e.getMessage());
      }
    }
  }

  public static void onPlayerQuit(String playerName) {
    recordPlayerExitTime(playerName);
  }

  public static List<String> getStoredIPs(LoginPlayer lp) {
    if (lp == null || lp.getIps() == null) {
      return new ArrayList<>();
    }
    try {
      return new ArrayList<>(Arrays.asList(lp.getIps().split(";")));
    } catch (Exception e) {
      return new ArrayList<>();
    }
  }

  public static boolean isFloodgatePlayer(String name) {
    Player player = Bukkit.getPlayerExact(name);
    return player != null && isFloodgatePlayer(player);
  }

  public static boolean isFloodgatePlayer(Player player) {
    try {
      return Bukkit.getPluginManager().getPlugin("floodgate") != null
          && FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
    } catch (Exception e) {
      return false;
    }
  }

  public static Long getLastLoginTime(String name) {
    LoginPlayer loginPlayer = PlayerCache.getIgnoreCase(name);
    return (loginPlayer != null) ? loginPlayer.getLastAction() : null;
  }

  public static void recordCurrentIP(Player player, LoginPlayer lp) {
    try {
      String currentIp = getPlayerIP(player);

      if (currentIp == null) {
        return;
      }

      List<String> ipsList =
          lp.getIpsList() != null ? new ArrayList<>(lp.getIpsList()) : new ArrayList<>();
      ipsList = ipsList.stream().distinct().collect(Collectors.toList());
      ipsList.remove(currentIp);
      ipsList.add(currentIp);
      while (ipsList.size() > MAX_STORED_IPS) {
        ipsList.remove(0);
      }
      lp.setIps(String.join(";", ipsList));

      savePlayerIPAsync(player, lp);
    } catch (Exception e) {
      BukkitContext.getLogger()
          .warning("Failed to record IP for player: " + player.getName() + " - " + e.getMessage());
    }
  }

  private static void savePlayerIPAsync(Player player, LoginPlayer lp) {
    CatScheduler.runTaskAsync(() -> savePlayerIP(lp));
  }

  private static void savePlayerIP(LoginPlayer lp) {
    try {
      BukkitContext.getSql().edit(lp);
      PlayerCache.refresh(lp.getName());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void sendBlankInventoryPacket(Player player) {
    if (!Config.Settings.emptyBackpack) return;

    try {
      ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
      PacketContainer inventoryPacket =
          protocolManager.createPacket(PacketType.Play.Server.WINDOW_ITEMS);
      inventoryPacket.getIntegers().write(0, 0);
      ItemStack[] blankInventory = new ItemStack[45];
      Arrays.fill(blankInventory, new ItemStack(Material.AIR));

      StructureModifier<ItemStack[]> itemArrayModifier = inventoryPacket.getItemArrayModifier();
      if (itemArrayModifier.size() > 0) {
        itemArrayModifier.write(0, blankInventory);
      } else {
        StructureModifier<List<ItemStack>> itemListModifier = inventoryPacket.getItemListModifier();
        itemListModifier.write(0, Arrays.asList(blankInventory));
      }

      protocolManager.sendServerPacket(player, inventoryPacket, false);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
