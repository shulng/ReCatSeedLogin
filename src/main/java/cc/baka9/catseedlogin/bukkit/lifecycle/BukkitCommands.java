package cc.baka9.catseedlogin.bukkit.lifecycle;

import cc.baka9.catseedlogin.bukkit.command.CommandBindEmail;
import cc.baka9.catseedlogin.bukkit.command.CommandCatSeedLogin;
import cc.baka9.catseedlogin.bukkit.command.CommandChangePassword;
import cc.baka9.catseedlogin.bukkit.command.CommandLogin;
import cc.baka9.catseedlogin.bukkit.command.CommandRegister;
import cc.baka9.catseedlogin.bukkit.command.CommandResetPassword;
import cc.baka9.catseedlogin.common.util.TabCompleteUtil;
import java.util.Arrays;
import java.util.Collections;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Bukkit 端指令注册器：集中绑定 plugin.yml 中声明指令的执行器与 TAB 补全。
 *
 * <p>与 BungeeCommands / VelocityCommands 职责对应，由 {@link BukkitStartup} 在启用流程中调用。
 */
public final class BukkitCommands {

  private BukkitCommands() {}

  /** 注册插件的全部指令。 */
  public static void registerAll(JavaPlugin plugin) {
    register(plugin, "login", new CommandLogin(), hint("密码"));
    register(plugin, "register", new CommandRegister(), hint("密码 重复密码"));
    register(plugin, "changepassword", new CommandChangePassword(), hint("旧密码 新密码 重复新密码"));
    register(plugin, "bindemail", new CommandBindEmail(), bindEmailHints());
    register(plugin, "resetpassword", new CommandResetPassword(), resetPasswordHints());

    CommandCatSeedLogin adminCommand = new CommandCatSeedLogin();
    register(plugin, "catseedlogin", adminCommand, adminCommand);
  }

  /** 绑定单个指令。指令未在 plugin.yml 中声明时记录警告并跳过，避免启用流程被中断。 */
  private static void register(
      JavaPlugin plugin, String name, CommandExecutor executor, TabCompleter completer) {
    PluginCommand command = plugin.getServer().getPluginCommand(name);
    if (command == null) {
      plugin.getLogger().warning("§c未在 plugin.yml 中找到指令: " + name);
      return;
    }
    command.setExecutor(executor);
    command.setTabCompleter(completer);
  }

  /** 仅在第一个参数位置提示固定占位文本的补全器。 */
  private static TabCompleter hint(String hint) {
    return (sender, command, alias, args) ->
        args.length == 1
            ? TabCompleteUtil.filter(Collections.singletonList(hint), args[0])
            : Collections.emptyList();
  }

  /** {@code /bindemail set 邮箱} 与 {@code /bindemail verify 验证码} 的补全器。 */
  private static TabCompleter bindEmailHints() {
    return (sender, command, alias, args) -> {
      if (args.length == 1) {
        return TabCompleteUtil.filter(Arrays.asList("set", "verify"), args[0]);
      }
      if (args.length == 2) {
        if ("set".equalsIgnoreCase(args[0])) {
          return TabCompleteUtil.filter(Collections.singletonList("需要绑定的邮箱"), args[1]);
        }
        if ("verify".equalsIgnoreCase(args[0])) {
          return TabCompleteUtil.filter(Collections.singletonList("邮箱获取的验证码"), args[1]);
        }
      }
      return Collections.emptyList();
    };
  }

  /** {@code /resetpassword forget} 与 {@code /resetpassword re 验证码 新密码} 的补全器。 */
  private static TabCompleter resetPasswordHints() {
    return (sender, command, alias, args) -> {
      if (args.length == 1) {
        return TabCompleteUtil.filter(Arrays.asList("forget", "re"), args[0]);
      }
      if (args.length == 2 && "re".equalsIgnoreCase(args[0])) {
        return TabCompleteUtil.filter(Collections.singletonList("验证码"), args[1]);
      }
      if (args.length == 3 && "re".equalsIgnoreCase(args[0])) {
        return TabCompleteUtil.filter(Collections.singletonList("新密码"), args[2]);
      }
      return Collections.emptyList();
    };
  }
}
