package cc.baka9.catseedlogin.bukkit

import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayer
import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayerHelper
import cc.baka9.catseedlogin.bukkit.`object`.TwoFactorState
import cc.baka9.catseedlogin.bukkit.task.Task
import cc.baka9.catseedlogin.bukkit.task.TaskAutoKick
import cc.baka9.catseedlogin.common.i18n.MessageKey
import java.util.regex.Pattern
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.geysermc.floodgate.api.FloodgateApi

class Listeners : Listener {
    private fun playerIsNotMinecraftPlayer(player: Player): Boolean =
        !player.javaClass.name.matches(Regex("org\\.bukkit\\.craftbukkit.*?\\.entity\\.CraftPlayer"))

    @EventHandler
    fun onPlayerCommandPreprocess(event: PlayerCommandPreprocessEvent) {
        val player = event.player
        if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.name)) return
        val input = event.message.lowercase()
        if (isTwoFactorCommand(input)) return
        for (regex: Pattern in Config.Settings.CommandWhiteList) {
            if (regex.matcher(input).matches()) return
        }
        event.isCancelled = true
    }

    private fun isTwoFactorCommand(input: String): Boolean =
        input == "/2fa" || input.startsWith("/2fa ") || input == "/twofa" || input.startsWith("/twofa ")

    @EventHandler
    fun onPlayerLogin(event: AsyncPlayerPreLoginEvent) {
        if (!Cache.isLoaded) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageKey.CACHE_NOT_INITIALIZED.get())
            return
        }
        val name = event.name
        val loginPlayer: LoginPlayer = Cache.getIgnoreCase(name) ?: return
        if (loginPlayer.name != name) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageKey.NAME_CASE_MISMATCH.get(loginPlayer.name))
            return
        }
        if (LoginPlayerHelper.isLogin(name)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageKey.PLAYER_ALREADY_ONLINE_ONLINE.get(loginPlayer.name))
            return
        }
        val address = event.address
        val hostAddress = address.hostAddress
        val count = Bukkit.getOnlinePlayers().count { onlinePlayer ->
            try {
                onlinePlayer.address?.address?.hostAddress == hostAddress
            } catch (_: Exception) {
                false
            }
        }
        if (!address.isLoopbackAddress && count >= Config.Settings.IpCountLimit) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageKey.TOO_MANY_SAME_IP.get())
        }
    }

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        if (Config.Settings.BeforeLoginAllowChat) return
        val player = event.player
        if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.name)) return
        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.name)) return
        event.isCancelled = true
    }

    @EventHandler
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (!LoginPlayerHelper.isLogin(event.player.name)) event.isCancelled = true
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        if (!LoginPlayerHelper.isLogin(player.name)) event.isCancelled = true
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val player = event.damager as? Player ?: return
        if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.name)) return
        event.isCancelled = true
    }

    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        if (!Config.Settings.BeforeLoginNoDamage) return
        val entity: Entity = event.entity
        val player = entity as? Player ?: return
        if (!playerIsNotMinecraftPlayer(player) && !LoginPlayerHelper.isLogin(entity.name)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        val player = event.player
        if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.name)) return
        val target = event.to ?: return
        if (Config.Settings.CanTpSpawnLocation && target == Config.Settings.SpawnLocation) return
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerGameModeChange(event: PlayerGameModeChangeEvent) {
        val player = event.player
        if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.name)) return
        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        val player = event.player
        if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.name)) return
        event.isCancelled = true
    }

    @EventHandler
    fun onEntityPickupItem(event: EntityPickupItemEvent) {
        val player = event.entity as? Player ?: return
        if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.name)) return
        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        if (playerIsNotMinecraftPlayer(player) || LoginPlayerHelper.isLogin(player.name)) return
        val from = event.from
        val to = event.to
        if (from.blockX == to.blockX && from.blockZ == to.blockZ && from.y - to.y >= 0.0) return
        if (Config.Settings.CanTpSpawnLocation) {
            CatScheduler.teleport(player, Config.Settings.SpawnLocation)
        } else {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        if (LoginPlayerHelper.isLogin(player.name)) {
            saveOfflineLocation(player)
            CatScheduler.runTaskLater(Runnable {
                try {
                    LoginPlayerHelper.remove(player.name)
                } catch (_: Exception) {
                    player.server.logger.warning("Failed to remove player on quit: ${player.name}")
                }
            }, Config.Settings.ReenterInterval)
        }
        try {
            (Task.getTaskAutoKick() as? TaskAutoKick)?.removePlayer(player.name)
        } catch (_: Exception) {
            player.server.logger.warning("Failed to remove player from auto-kick list: ${player.name}")
        }
    }

    private fun saveOfflineLocation(player: Player) {
        try {
            if (!player.isDead || Config.Settings.DeathStateQuitRecordLocation) Config.setOfflineLocation(player)
        } catch (_: Exception) {
            player.server.logger.warning("保存玩家离线位置失败: ${player.name}")
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        if (TwoFactorState.isRebindRequired(player.name)) {
            player.sendMessage(MessageKey.TWO_FACTOR_REBIND_REQUIRED.get())
        }
        if (Config.Settings.BedrockLoginBypass && LoginPlayerHelper.isFloodgatePlayer(player)) {
            player.sendMessage(Config.Language.BEDROCK_LOGIN_BYPASS)
            return
        }
        if (Config.Settings.LoopbackLoginBypass && LoginPlayerHelper.isLoopbackPlayer(player)) {
            Cache.getIgnoreCase(player.name)?.let { LoginPlayerHelper.add(it) }
            player.sendMessage(Config.Language.LOOPBACK_LOGIN_BYPASS)
            teleportToLastLocation(player)
            return
        }
        if (Config.Settings.LoginwiththesameIP && LoginPlayerHelper.recordCurrentIP(player)) {
            Cache.getIgnoreCase(player.name)?.let { LoginPlayerHelper.add(it) }
            player.sendMessage(Config.Language.LOGIN_WITH_THE_SAME_IP)
            teleportToLastLocation(player)
            return
        }
        Cache.refresh(player.name)
        if (Config.Settings.CanTpSpawnLocation) CatScheduler.teleport(player, Config.Settings.SpawnLocation)
    }

    private fun teleportToLastLocation(player: Player) {
        if (!Config.Settings.AfterLoginBack || !Config.Settings.CanTpSpawnLocation) return
        val playerId = player.uniqueId
        CatScheduler.runTaskAsync {
            val location = Config.getOfflineLocation(player.name)
            if (location.isPresent) CatScheduler.runTaskLater(Runnable {
                val online = Bukkit.getPlayer(playerId)
                if (online != null && online.isOnline) CatScheduler.teleport(online, location.get())
            }, 1L)
        }
    }

    @EventHandler
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        val name = event.name
        val validName = try { name.matches(Regex(Config.Settings.NamePattern)) } catch (_: Exception) { false }
        if (Config.Settings.LimitChineseID && !validName) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageKey.INVALID_NAME_PATTERN.get())
            return
        }
        if (checkFloodgatePrefixProtect(event, name)) return
        if (name.length < Config.Settings.MinLengthID) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageKey.NAME_TOO_SHORT.get(Config.Settings.MinLengthID))
        } else if (name.length > Config.Settings.MaxLengthID) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageKey.NAME_TOO_LONG.get(Config.Settings.MaxLengthID))
        }
    }

    private fun checkFloodgatePrefixProtect(event: AsyncPlayerPreLoginEvent, name: String): Boolean {
        if (!Config.Settings.FloodgatePrefixProtect || Bukkit.getPluginManager().getPlugin("floodgate") == null) return false
        return try {
            val prefix = FloodgateApi.getInstance().playerPrefix
            if (name.startsWith(prefix, ignoreCase = true) && !FloodgateApi.getInstance().isFloodgatePlayer(event.uniqueId)) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MessageKey.ILLEGAL_BEDROCK_NAME.get(prefix))
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
