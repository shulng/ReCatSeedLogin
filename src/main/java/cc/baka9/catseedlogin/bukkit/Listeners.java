package cc.baka9.catseedlogin.bukkit;

import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.geysermc.floodgate.api.FloodgateApi;

import cc.baka9.catseedlogin.bukkit.task.Task;
import cc.baka9.catseedlogin.bukkit.task.TaskAutoKick;
import cc.baka9.catseedlogin.bukkit.Cache;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import cc.baka9.catseedlogin.common.model.LoginPlayer;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;

public class Listeners implements Listener {

    private boolean playerIsNotMinecraftPlayer(Player p) {
        return !p.getClass().getName().matches("org\\.bukkit\\.craftbukkit.*?\\.entity\\.CraftPlayer");
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.getName())) return;
        String input = event.getMessage().toLowerCase();
        for (Pattern regex : Config.Settings.CommandWhiteList) {
            if (regex.matcher(input).find()) return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        String name = event.getName();

        // Name validation
        if (Config.Settings.LimitChineseID && !name.matches(Config.Settings.NamePattern)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageKey.INVALID_NAME_PATTERN.get());
            return;
        }
        if (checkFloodgatePrefixProtect(event, name)) return;
        if (name.length() < Config.Settings.MinLengthID) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageKey.NAME_TOO_SHORT.get(Config.Settings.MinLengthID));
            return;
        } else if (name.length() > Config.Settings.MaxLengthID) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageKey.NAME_TOO_LONG.get(Config.Settings.MaxLengthID));
            return;
        }

        // Cache and IP validation
        if (!Cache.isLoaded) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageKey.CACHE_NOT_INITIALIZED.get());
            return;
        }
        LoginPlayer lp = Cache.getIgnoreCase(name);
        if (lp == null) return;
        if (!lp.getName().equals(name)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageKey.NAME_CASE_MISMATCH.get(lp.getName()));
            return;
        }
        if (LoginPlayerHelper.isLogin(name)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageKey.PLAYER_ALREADY_ONLINE_ONLINE.get(lp.getName()));
            return;
        }
        String hostAddress = event.getAddress().getHostAddress();
        long count = Bukkit.getOnlinePlayers().stream()
                .filter(p -> {
                    try {
                        return p.getAddress() != null && p.getAddress().getAddress().getHostAddress().equals(hostAddress);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count();
        if (!event.getAddress().isLoopbackAddress() && count >= Config.Settings.IpCountLimit) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageKey.TOO_MANY_SAME_IP.get());
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
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
        if (!(event.getWhoClicked() instanceof Player) || LoginPlayerHelper.isLogin(event.getWhoClicked().getName())) return;
        event.setCancelled(true);
    }

    //登陆之前不能攻击
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.getName())) return;
        event.setCancelled(true);
    }

    //登陆之前不会受到伤害
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!Config.Settings.BeforeLoginNoDamage) return;
        Entity entity = event.getEntity();
        if (entity instanceof Player && !playerIsNotMinecraftPlayer((Player) entity) && !LoginPlayerHelper.isLogin(entity.getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.getName())) return;
        if (event.getTo() == null) return;
        if (Config.Settings.CanTpSpawnLocation && event.getTo().equals(Config.Settings.SpawnLocation)) return;
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
        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ() && from.getY() - to.getY() >= 0.0D) {
            return;
        }
        if (Config.Settings.CanTpSpawnLocation) {
            CatScheduler.teleport(player, Config.Settings.SpawnLocation);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (LoginPlayerHelper.isLogin(player.getName())) {
            saveOfflineLocation(player);
            CatScheduler.runTaskLater(() -> {
                try {
                    LoginPlayerHelper.remove(player.getName());
                } catch (Exception e) {
                    player.getServer().getLogger().warning("Failed to remove player on quit: " + player.getName());
                }
            }, Config.Settings.ReenterInterval);
        }
        try {
            TaskAutoKick task = Task.getTaskAutoKick();
            if (task != null) {
                task.removePlayer(player.getName());
            }
        } catch (Exception e) {
            player.getServer().getLogger().warning("Failed to remove player from auto-kick list: " + player.getName());
        }
    }

    private void saveOfflineLocation(Player player) {
        try {
            if (!player.isDead() || Config.Settings.DeathStateQuitRecordLocation) {
                Config.setOfflineLocation(player);
            }
        } catch (Exception e) {
            player.getServer().getLogger().warning("保存玩家离线位置失败: " + player.getName());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (Config.Settings.BedrockLoginBypass && LoginPlayerHelper.isFloodgatePlayer(player)) {
            player.sendMessage(Config.Language.BEDROCK_LOGIN_BYPASS);
            return;
        }
        if (Config.Settings.LoginwiththesameIP && LoginPlayerHelper.recordCurrentIP(player)) {
            player.sendMessage(Config.Language.LOGIN_WITH_THE_SAME_IP);
            teleportToLastLocation(player);
            return;
        }
        Cache.refresh(player.getName());
        if (Config.Settings.CanTpSpawnLocation) {
            CatScheduler.teleport(player, Config.Settings.SpawnLocation);
        }
    }

    private void teleportToLastLocation(Player player) {
        if (!Config.Settings.AfterLoginBack || !Config.Settings.CanTpSpawnLocation) return;
        Config.getOfflineLocation(player).ifPresent(location ->
                CatScheduler.runTaskLater(() -> CatScheduler.teleport(player, location), 1L)
        );
    }

    private boolean checkFloodgatePrefixProtect(AsyncPlayerPreLoginEvent event, String name) {
        if (!Config.Settings.FloodgatePrefixProtect || Bukkit.getPluginManager().getPlugin("floodgate") == null) {
            return false;
        }
        try {
            String prefix = FloodgateApi.getInstance().getPlayerPrefix();
            if (name.toLowerCase().startsWith(prefix.toLowerCase()) && !FloodgateApi.getInstance().isFloodgatePlayer(event.getUniqueId())) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageKey.ILLEGAL_BEDROCK_NAME.get(prefix));
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
