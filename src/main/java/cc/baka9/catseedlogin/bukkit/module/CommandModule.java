package cc.baka9.catseedlogin.bukkit.module;

import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.bukkit.command.*;
import cc.baka9.catseedlogin.common.util.TabCompleteUtil;
import java.util.Arrays;
import java.util.Collections;
import org.bukkit.command.PluginCommand;

public final class CommandModule implements BukkitModule {
  @Override
  public void enable(CatSeedLogin plugin) {
    register(plugin, "login", new CommandLogin(), (sender, command, alias, args) -> args.length == 1
        ? TabCompleteUtil.filter(Collections.singletonList("密码"), args[0]) : Collections.emptyList());
    register(plugin, "register", new CommandRegister(), (sender, command, alias, args) -> args.length == 1
        ? TabCompleteUtil.filter(Collections.singletonList("密码 重复密码"), args[0]) : Collections.emptyList());
    register(plugin, "changepassword", new CommandChangePassword(), (sender, command, alias, args) -> args.length == 1
        ? TabCompleteUtil.filter(Collections.singletonList("旧密码 新密码 重复新密码"), args[0]) : Collections.emptyList());
    register(plugin, "bindemail", new CommandBindEmail(), (sender, command, alias, args) -> {
      if (args.length == 1) return TabCompleteUtil.filter(Arrays.asList("set", "verify"), args[0]);
      if (args.length == 2 && "set".equalsIgnoreCase(args[0])) return TabCompleteUtil.filter(Collections.singletonList("需要绑定的邮箱"), args[1]);
      if (args.length == 2 && "verify".equalsIgnoreCase(args[0])) return TabCompleteUtil.filter(Collections.singletonList("邮箱获取的验证码"), args[1]);
      return Collections.emptyList();
    });
    register(plugin, "resetpassword", new CommandResetPassword(), (sender, command, alias, args) -> {
      if (args.length == 1) return TabCompleteUtil.filter(Arrays.asList("forget", "re"), args[0]);
      if (args.length == 2 && "re".equalsIgnoreCase(args[0])) return TabCompleteUtil.filter(Collections.singletonList("验证码"), args[1]);
      if (args.length == 3 && "re".equalsIgnoreCase(args[0])) return TabCompleteUtil.filter(Collections.singletonList("新密码"), args[2]);
      return Collections.emptyList();
    });
    CommandTwoFactor twoFactor = new CommandTwoFactor();
    register(plugin, "2fa", twoFactor, twoFactor);
    CommandCatSeedLogin admin = new CommandCatSeedLogin();
    register(plugin, "catseedlogin", admin, admin);
  }

  private void register(CatSeedLogin plugin, String name, org.bukkit.command.CommandExecutor executor,
                        org.bukkit.command.TabCompleter completer) {
    PluginCommand command = plugin.getServer().getPluginCommand(name);
    if (command != null) { command.setExecutor(executor); command.setTabCompleter(completer); }
  }
}
