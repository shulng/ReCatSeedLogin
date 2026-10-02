package cc.baka9.catseedlogin.bukkit.listener;

import cc.baka9.catseedlogin.bukkit.BukkitContext;
import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.database.OfflineLocationStore;
import cc.baka9.catseedlogin.bukkit.scheduler.CatScheduler;
import cc.baka9.catseedlogin.bukkit.session.LoginPlayerHelper;
import cc.baka9.catseedlogin.bukkit.task.Task;
import cc.baka9.catseedlogin.bukkit.task.TaskAutoKick;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import cc.baka9.catseedlogin.common.model.LoginPlayer;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.geysermc.floodgate.api.FloodgateApi;

/** 登录前行为限制：在玩家完成登录前拦截移动、交互、指令、聊天、伤害等行为。 */
public class BukkitListeners implements Listener {

  private BukkitConfigManager config() {
    return BukkitContext.getConfigManager();
  }

  /** 排除非原版玩家实现（如 NPC 插件的假人）。 */
  private boolean playerIsNotMinecraftPlayer(Player p) {
    return !p.getClass().getName().matches("org\\.bukkit\\.craftbukkit.*?\\.entity\\.CraftPlayer");
  }

  private boolean isLoggedIn(Player player) {
    return !playerIsNotMinecraftPlayer(player) && LoginPlayerHelper.isLogin(player.getName());
  }

  @EventHandler
  public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
    Player player = event.getPlayer();
    if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.getName())) return;
    String input = event.getMessage().toLowerCase();
    for (Pattern regex : config().getCommandWhiteList()) {
      if (regex.matcher(input).find()) return;
    }
    event.setCancelled(true);
  }

  @EventHandler
  public void onAsyncPlayerPreLoginCacheCheck(AsyncPlayerPreLoginEvent event) {
    if (!Cache.isLoaded) {
      event.disallow(
          AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageKey.CACHE_NOT_INITIALIZED.get());
      return;
    }
    String name = event.getName();
    LoginPlayer lp = Cache.getIgnoreCase(name);
    if (lp == null) return;
    if (!lp.getName().equals(name)) {
      event.disallow(
          AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
          MessageKey.NAME_CASE_MISMATCH.get(lp.getName()));
      return;
    }
    if (LoginPlayerHelper.isLogin(name)) {
      event.disallow(
          AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
          MessageKey.PLAYER_ALREADY_ONLINE_ONLINE.get(lp.getName()));
      return;
    }
    String hostAddress = event.getAddress().getHostAddress();
    long count =
        Bukkit.getOnlinePlayers().stream()
            .filter(
                p -> {
                  try {
                    return p.getAddress() != null
                        && p.getAddress().getAddress().getHostAddress().equals(hostAddress);
                  } catch (Exception e) {
                    return false;
                  }
                })
            .count();
    if (!event.getAddress().isLoopbackAddress() && count >= config().getIpCountLimit()) {
      event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageKey.TOO_MANY_SAME_IP.get());
    }
  }

  @EventHandler
  public void onPlayerChat(AsyncPlayerChatEvent event) {
    if (config().isBeforeLoginAllowChat()) return;
    Player player = event.getPlayer();
    if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.getName())) return;
    event.setCancelled(true);
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.getName())) return;
    event.setCancelled(true);
  }

  @EventHandler
  public void onInventoryOpen(InventoryOpenEvent event) {
    if (LoginPlayerHelper.isLogin(event.getPlayer().getName())) return;
    event.setCancelled(true);
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player)
        || LoginPlayerHelper.isLogin(event.getWhoClicked().getName())) return;
    event.setCancelled(true);
  }

  // 登录之前不能攻击
  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player)) return;
    Player player = (Player) event.getDamager();
    if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.getName())) return;
    event.setCancelled(true);
  }

  // 登录之前不会受到伤害
  @EventHandler
  public void onEntityDamage(EntityDamageEvent event) {
    if (!config().isBeforeLoginNoDamage()) return;
    Entity entity = event.getEntity();
    if (entity instanceof Player && !isLoggedIn((Player) entity)) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerTeleport(PlayerTeleportEvent event) {
    Player player = event.getPlayer();
    if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.getName())) return;
    if (event.getTo() == null) return;
    if (config().isCanTpSpawnLocation()
        && event.getTo().equals(config().getBukkitSpawnLocation())) {
      return;
    }
    event.setCancelled(true);
  }

  // 登陆之前不能切换游戏模式 (防止使用 F3+F4 游戏模式切换器绕过限制)
  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
    Player player = event.getPlayer();
    if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.getName())) return;
    event.setCancelled(true);
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    Player player = event.getPlayer();
    if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.getName())) return;
    event.setCancelled(true);
  }

  @EventHandler
  public void onEntityPickupItem(EntityPickupItemEvent event) {
    if (!(event.getEntity() instanceof Player)) return;
    Player player = (Player) event.getEntity();
    if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.getName())) return;
    event.setCancelled(true);
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    Player player = event.getPlayer();
    if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.getName())) return;
    Location from = event.getFrom();
    Location to = event.getTo();
    if (to == null) {
      event.setCancelled(true);
      return;
    }
    if (from.getBlockX() == to.getBlockX()
        && from.getBlockZ() == to.getBlockZ()
        && from.getY() - to.getY() >= 0.0D) {
      return;
    }
    if (config().isCanTpSpawnLocation()) {
      CatScheduler.teleport(player, config().getBukkitSpawnLocation());
    } else {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    if (LoginPlayerHelper.isLogin(player.getName())) {
      saveOfflineLocation(player);
      CatScheduler.runTaskLater(
          () -> {
            try {
              LoginPlayerHelper.remove(player.getName());
            } catch (Exception e) {
              player
                  .getServer()
                  .getLogger()
                  .warning("Failed to remove player on quit: " + player.getName());
            }
          },
          config().getReenterInterval());
    }
    try {
      TaskAutoKick task = Task.getTaskAutoKick();
      if (task != null) {
        task.removePlayer(player.getName());
      }
    } catch (Exception e) {
      player
          .getServer()
          .getLogger()
          .warning("Failed to remove player from auto-kick list: " + player.getName());
    }
  }

  private void saveOfflineLocation(Player player) {
    try {
      if (!player.isDead() || config().isDeathStateQuitRecordLocation()) {
        OfflineLocationStore.saveAsync(player);
      }
    } catch (Exception e) {
      player.getServer().getLogger().warning("保存玩家离线位置失败: " + player.getName());
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    if (config().isBedrockLoginBypass() && LoginPlayerHelper.isFloodgatePlayer(player)) {
      player.sendMessage(MessageKey.BEDROCK_LOGIN_BYPASS.get());
      return;
    }
    if (bypassByLoopback(player)) {
      return;
    }
    if (bypassBySameIp(player)) {
      return;
    }
    Cache.refresh(player.getName());
    if (config().isCanTpSpawnLocation()) {
      CatScheduler.teleport(player, config().getBukkitSpawnLocation());
    }
  }

  private boolean bypassByLoopback(Player player) {
    if (!config().isLoopbackLoginBypass() || !LoginPlayerHelper.isLoopbackPlayer(player)) {
      return false;
    }
    markLoggedIn(player);
    player.sendMessage(MessageKey.LOOPBACK_LOGIN_BYPASS.get());
    teleportToLastLocation(player);
    return true;
  }

  private boolean bypassBySameIp(Player player) {
    if (!config().isLoginWithSameIP() || !LoginPlayerHelper.recordCurrentIP(player)) {
      return false;
    }
    markLoggedIn(player);
    player.sendMessage(MessageKey.LOGIN_WITH_THE_SAME_IP.get());
    teleportToLastLocation(player);
    return true;
  }

  private void markLoggedIn(Player player) {
    LoginPlayer lp = Cache.getIgnoreCase(player.getName());
    if (lp != null) {
      LoginPlayerHelper.add(lp);
    }
  }

  private void teleportToLastLocation(Player player) {
    if (!config().isAfterLoginBack() || !config().isCanTpSpawnLocation()) return;
    OfflineLocationStore.get(player)
        .ifPresent(
            location ->
                CatScheduler.runTaskLater(() -> CatScheduler.teleport(player, location), 1L));
  }

  // id只能下划线字母数字
  @EventHandler
  public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
    String name = event.getName();
    if (config().isLimitChineseID() && !name.matches(config().getNamePattern())) {
      event.disallow(
          AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageKey.INVALID_NAME_PATTERN.get());
      return;
    }
    if (checkFloodgatePrefixProtect(event, name)) return;
    if (name.length() < config().getMinLengthID()) {
      event.disallow(
          AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
          MessageKey.NAME_TOO_SHORT.get(config().getMinLengthID()));
    } else if (name.length() > config().getMaxLengthID()) {
      event.disallow(
          AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
          MessageKey.NAME_TOO_LONG.get(config().getMaxLengthID()));
    }
  }

  private boolean checkFloodgatePrefixProtect(AsyncPlayerPreLoginEvent event, String name) {
    if (!config().isFloodgatePrefixProtect()
        || Bukkit.getPluginManager().getPlugin("floodgate") == null) {
      return false;
    }
    try {
      String prefix = FloodgateApi.getInstance().getPlayerPrefix();
      if (name.toLowerCase().startsWith(prefix.toLowerCase())
          && !FloodgateApi.getInstance().isFloodgatePlayer(event.getUniqueId())) {
        event.disallow(
            AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
            MessageKey.ILLEGAL_BEDROCK_NAME.get(prefix));
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }
}
