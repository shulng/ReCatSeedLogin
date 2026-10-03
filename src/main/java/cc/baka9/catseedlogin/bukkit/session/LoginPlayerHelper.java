package cc.baka9.catseedlogin.bukkit.session;

import cc.baka9.catseedlogin.bukkit.BukkitContext;
import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.lifecycle.BukkitHooks;
import cc.baka9.catseedlogin.bukkit.scheduler.CatScheduler;
import cc.baka9.catseedlogin.common.model.LoginPlayer;
import cc.baka9.catseedlogin.common.util.ValidationUtil;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.geysermc.floodgate.api.FloodgateApi;

/** 登录会话管理：维护当前已登录玩家集合、退出时间，并提供免登录判定与 IP 记录能力。 */
public final class LoginPlayerHelper {

  private static final Map<String, LoginPlayer> loginPlayers = new ConcurrentHashMap<>();
  private static final Map<String, Long> playerExitTimes = new ConcurrentHashMap<>();
  private static final int MAX_STORED_IPS = 10;

  private LoginPlayerHelper() {}

  private static BukkitConfigManager config() {
    return BukkitContext.getConfigManager();
  }

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
    if (lp != null) {
      remove(lp.getName());
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

  /** 满足基岩版绕过、同 IP 免登录或本地回环免登录时视为已登录。 */
  private static boolean canBypassLogin(String name) {
    return (config().isBedrockLoginBypass() && isFloodgatePlayer(name))
        || (config().isLoginWithSameIP() && recordCurrentIP(name))
        || (config().isLoopbackLoginBypass() && isLoopbackPlayer(name));
  }

  public static boolean isRegister(String name) {
    return (config().isBedrockLoginBypass() && isFloodgatePlayer(name))
        || Cache.getIgnoreCase(name) != null;
  }

  public static boolean recordCurrentIP(String name) {
    Player player = Bukkit.getPlayerExact(name);
    return player != null && recordCurrentIP(player);
  }

  /** 判断玩家当前 IP 是否在 IP 免登录有效期内。 */
  public static boolean recordCurrentIP(Player player) {
    String currentIP = getPlayerIP(player);
    if (currentIP == null) return false;

    LoginPlayer storedPlayer = Cache.getIgnoreCase(player.getName());
    if (storedPlayer == null) return false;

    if (ValidationUtil.isLoopbackAddress(currentIP)) return false;

    List<String> storedIPs = storedPlayer.getIpsList();
    int ipTimeout = config().getIPTimeout();
    if (ipTimeout == 0) {
      return storedIPs.contains(currentIP);
    }
    Long exitTime = playerExitTimes.get(player.getName());
    return exitTime != null
        && storedIPs.contains(currentIP)
        && (System.currentTimeMillis() - exitTime) <= (long) ipTimeout * 60 * 1000;
  }

  private static String getPlayerIP(Player player) {
    if (player == null) return null;
    return Optional.ofNullable(player.getAddress())
        .map(addr -> addr.getAddress())
        .map(InetAddress::getHostAddress)
        .orElse(null);
  }

  public static void onPlayerQuit(String playerName) {
    if (playerName == null) return;
    if (config().getIPTimeout() != 0 && isLogin(playerName)) {
      try {
        playerExitTimes.put(playerName, System.currentTimeMillis());
      } catch (Exception e) {
        BukkitContext.getLogger()
            .severe("Failed to record player exit time: " + playerName + " - " + e.getMessage());
      }
    }
  }

  /** 判断玩家是否从本地回环地址(127.0.0.1 / ::1 / localhost)连接。 */
  public static boolean isLoopbackPlayer(String name) {
    Player player = Bukkit.getPlayerExact(name);
    return player != null && isLoopbackPlayer(player);
  }

  public static boolean isLoopbackPlayer(Player player) {
    String ip = getPlayerIP(player);
    return ip != null && ValidationUtil.isLoopbackAddress(ip);
  }

  public static boolean isFloodgatePlayer(String name) {
    Player player = Bukkit.getPlayerExact(name);
    return player != null && isFloodgatePlayer(player);
  }

  public static boolean isFloodgatePlayer(Player player) {
    try {
      return BukkitHooks.isFloodgateEnabled()
          && FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
    } catch (Exception e) {
      return false;
    }
  }

  public static Long getLastLoginTime(String name) {
    LoginPlayer loginPlayer = Cache.getIgnoreCase(name);
    return (loginPlayer != null) ? loginPlayer.getLastAction() : null;
  }

  /** 把玩家当前 IP 记入账号（最多保留 {@value #MAX_STORED_IPS} 条），并异步落库。 */
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

      CatScheduler.runTaskAsync(() -> savePlayerIP(lp));
    } catch (Exception e) {
      BukkitContext.getLogger()
          .warning("Failed to record IP for player: " + player.getName() + " - " + e.getMessage());
    }
  }

  private static void savePlayerIP(LoginPlayer lp) {
    try {
      BukkitContext.getSql().edit(lp);
      Cache.refresh(lp.getName());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** 向未登录玩家发送空背包数据包，避免背包内容泄露（需要 ProtocolLib）。 */
  public static void sendBlankInventoryPacket(Player player) {
    if (!config().isEmptyBackpack()) return;

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
