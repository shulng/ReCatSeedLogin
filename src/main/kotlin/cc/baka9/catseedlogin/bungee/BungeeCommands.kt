package cc.baka9.catseedlogin.bungee

import cc.baka9.catseedlogin.bungee.config.BungeeConfigManager
import cc.baka9.catseedlogin.common.i18n.MessageKey
import cc.baka9.catseedlogin.common.util.TabCompleteUtil
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.plugin.TabExecutor

class BungeeCommands(
    name: String,
    permission: String,
    private val configManager: BungeeConfigManager,
    vararg aliases: String
) : net.md_5.bungee.api.plugin.Command(name, permission, *aliases), TabExecutor {
    companion object {
        @JvmField
        val SUB_COMMANDS: List<String> = listOf("reload")
    }

    override fun execute(commandSender: CommandSender?, args: Array<out String>?) {
        if (commandSender == null || args == null || args.isEmpty()) return
        try {
            if (args[0].equals("reload", ignoreCase = true)) {
                configManager.reload()
                commandSender.sendMessage(TextComponent(MessageKey.CONFIG_RELOADED.get()))
            }
        } catch (e: Exception) {
            commandSender.sendMessage(TextComponent("§c指令执行时出错: ${e.message}"))
        }
    }

    override fun onTabComplete(commandSender: CommandSender?, args: Array<out String>?): Iterable<String> {
        if (commandSender == null || !commandSender.hasPermission(permission)) return emptyList()
        if (args == null || args.size != 1) return emptyList()
        return TabCompleteUtil.filter(SUB_COMMANDS, args[0])
    }
}
