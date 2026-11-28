package cc.baka9.catseedlogin.bungee;

import cc.baka9.catseedlogin.bungee.config.BungeeConfigManager;
import cc.baka9.catseedlogin.common.i18n.MessageKey;
import cc.baka9.catseedlogin.common.util.TabCompleteUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.TabExecutor;

public class BungeeCommands extends net.md_5.bungee.api.plugin.Command implements TabExecutor {

  /** 所有管理子命令，执行分发与 TAB 补全共用同一份数据源。 */
  public static final List<String> SUB_COMMANDS =
      Collections.unmodifiableList(Arrays.asList("reload"));

  private final BungeeConfigManager configManager;

  public BungeeCommands(
      String name, String permission, BungeeConfigManager configManager, String... aliases) {
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

  @Override
  public Iterable<String> onTabComplete(CommandSender commandSender, String[] args) {
    if (commandSender == null || !commandSender.hasPermission(getPermission())) {
      return Collections.emptyList();
    }
    if (args == null || args.length != 1) {
      return Collections.emptyList();
    }
    return TabCompleteUtil.filter(SUB_COMMANDS, args[0]);
  }
}
