package cc.baka9.catseedlogin.bukkit.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.entity.Player

object TwoFactorAdventureSender {
    @JvmStatic fun send(player: Player, lines: List<TwoFactorMessageUtil.MessageLine>) {
        for (line in lines) {
            var component = Component.text(line.text)
            line.copyText?.let { component = component.clickEvent(ClickEvent.copyToClipboard(it)) }
            line.hoverText?.let { component = component.hoverEvent(HoverEvent.showText(Component.text(it))) }
            player.sendMessage(component)
        }
    }
}
