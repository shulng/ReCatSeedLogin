package cc.baka9.catseedlogin.bungee;

import cc.baka9.catseedlogin.bungee.config.BungeeConfigManager;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class BungeeCommands extends net.md_5.bungee.api.plugin.Command {

    private final BungeeConfigManager configManager;

    public BungeeCommands(String name, String permission, BungeeConfigManager configManager, String... aliases) {
        super(name, permission, aliases);
        this.configManager = configManager;
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (commandSender == null || args == null || args.length == 0) {
            return;
        }
        try {
            if (args[0].equalsIgnoreCase("reload")) {
                configManager.reload();
                commandSender.sendMessage(new TextComponent(MessageKey.CONFIG_RELOADED.get()));
            }
        } catch (Exception e) {
            commandSender.sendMessage(new TextComponent("§c指令执行时出错: " + e.getMessage()));
        }
    }
}