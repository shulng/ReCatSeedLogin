package cc.baka9.catseedlogin.bukkit;

import cc.baka9.catseedlogin.bukkit.event.CatSeedPlayerLoginEvent;
import cc.baka9.catseedlogin.bukkit.event.CatSeedPlayerRegisterEvent;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 登录前给未登录玩家施加失明效果，登录或注册成功后移除。
 *
 * <p>合并自 CatSeedLoginBlinding: https://github.com/CatSeed/CatSeedLoginBlinding
 */
public class BlindingListeners implements Listener {

  /** 24 小时 (单位: tick)，沿用 CatSeedLoginBlinding 原本的时长。 */
  private static final int BLINDNESS_DURATION = 20 * 60 * 60 * 24;

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    if (!Config.Settings.BlindingBeforeLogin) return;
    Player player = event.getPlayer();
    if (LoginPlayerHelper.isLogin(player.getName())) return;
    addBlindness(player);
  }

  @EventHandler
  public void onPlayerLogin(CatSeedPlayerLoginEvent event) {
    if (event.getResult() != CatSeedPlayerLoginEvent.Result.SUCCESS) return;
    removeBlindness(event.getPlayer());
  }

  @EventHandler
  public void onPlayerRegister(CatSeedPlayerRegisterEvent event) {
    removeBlindness(event.getPlayer());
  }

  @SuppressWarnings("deprecation")
  private void addBlindness(Player player) {
    player.addPotionEffect(
        new PotionEffect(PotionEffectType.BLINDNESS, BLINDNESS_DURATION, 0), true);
  }

  private void removeBlindness(Player player) {
    if (player == null) return;
    player.removePotionEffect(PotionEffectType.BLINDNESS);
  }
}
