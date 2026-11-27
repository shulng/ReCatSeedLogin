package cc.baka9.catseedlogin.bukkit.util

import cc.baka9.catseedlogin.bukkit.`object`.LoginPlayerHelper
import org.bukkit.entity.Player

object TwoFactorMessageUtil {
    private const val ADVENTURE_COMPONENT_CLASS = "net.kyori.adventure.text.Component"
    private const val ADVENTURE_SENDER_CLASS = "cc.baka9.catseedlogin.bukkit.util.TwoFactorAdventureSender"
    @Volatile private var adventureAvailable = false

    class MessageLine(text: String?, val copyText: String? = null, val hoverText: String? = null) {
        val text: String = text ?: ""
    }

    @JvmStatic fun probe() { adventureAvailable = detectAdventure() }
    @JvmStatic fun isAdventureAvailable() = adventureAvailable
    @JvmStatic fun send(player: Player?, lines: List<MessageLine>?) {
        if (player == null || lines.isNullOrEmpty()) return
        if (!adventureAvailable || LoginPlayerHelper.isFloodgatePlayer(player)) {
            sendPlain(player, lines); return
        }
        sendComponent(player, lines)
    }
    @JvmStatic fun sendText(player: Player, text: String) = send(player, listOf(MessageLine(text)))
    private fun detectAdventure(): Boolean = try { Class.forName(ADVENTURE_COMPONENT_CLASS); true } catch (_: Throwable) { false }
    private fun sendPlain(player: Player, lines: List<MessageLine>) { lines.forEach { player.sendMessage(it.text) } }
    private fun sendComponent(player: Player, lines: List<MessageLine>) {
        try { Class.forName(ADVENTURE_SENDER_CLASS).getMethod("send", Player::class.java, List::class.java).invoke(null, player, lines) }
        catch (_: Throwable) { sendPlain(player, lines) }
    }
}
