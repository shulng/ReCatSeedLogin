package cc.baka9.catseedlogin.bukkit

import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayerHelper
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import org.bukkit.entity.Player

class ProtocolLibListeners : PacketAdapter(
    requireNotNull(CatSeedLogin.instance),
    ListenerPriority.HIGHEST,
    PacketType.Play.Server.SET_SLOT,
    PacketType.Play.Server.WINDOW_ITEMS
) {
    override fun onPacketSending(event: PacketEvent) {
        val player: Player = event.player
        val windowId = event.packet.integers.read(0)
        if (windowId == 0 && !LoginPlayerHelper.isLogin(player.name)) {
            event.isCancelled = true
        }
    }

    companion object {
        @JvmStatic
        fun enable() {
            ProtocolLibrary.getProtocolManager().addPacketListener(ProtocolLibListeners())
        }
    }
}
