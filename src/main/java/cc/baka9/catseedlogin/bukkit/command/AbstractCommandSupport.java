package cc.baka9.catseedlogin.bukkit.command;

import cc.baka9.catseedlogin.bukkit.config.Config;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** 给玩家(Player)执行命令的基类，统一处理非玩家守卫与 Floodgate 基岩登录跳过。 */
public abstract class AbstractCommandSupport implements CommandExecutor {

  @Override
  public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      return false;
    }
    Player player = (Player) sender;
    if (isBedrockBypassed(player)) {
      return true;
    }
    return onPlayerCommand(player, args);
  }

  /** 玩家命令的实际逻辑；已通过非玩家守卫与 Floodgate 跳过校验。 */
  protected abstract boolean onPlayerCommand(Player player, String[] args);

  /** 基岩版(Floodgate)玩家登录跳过判断。 */
  protected final boolean isBedrockBypassed(Player player) {
    return Config.Settings.BedrockLoginBypass && LoginPlayerHelper.isFloodgatePlayer(player);
  }
}