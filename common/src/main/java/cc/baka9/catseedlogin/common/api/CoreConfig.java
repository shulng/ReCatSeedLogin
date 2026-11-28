package cc.baka9.catseedlogin.common.api;

import java.util.List;
import java.util.regex.Pattern;

public interface CoreConfig {

  int getIpRegisterCountLimit();

  int getIpCountLimit();

  boolean isLimitChineseID();

  boolean isBedrockLoginBypass();

  boolean isLoginWithSameIP();

  boolean isEmptyBackpack();

  int getIPTimeout();

  int getMaxLengthID();

  int getMinLengthID();

  boolean isBeforeLoginNoDamage();

  long getReenterInterval();

  boolean isAfterLoginBack();

  boolean isCanTpSpawnLocation();

  int getAutoKick();

  String getNamePattern();

  boolean isDeathStateQuitRecordLocation();

  boolean isFloodgatePrefixProtect();

  /** 本地回环地址(127.0.0.1 / ::1)连接时是否跳过登录。 */
  boolean isLoopbackLoginBypass();

  /** 登录前是否允许发消息。 */
  boolean isBeforeLoginAllowChat();

  /** 登录前是否给未登录玩家施加失明效果。 */
  boolean isBlindingBeforeLogin();

  List<Pattern> getCommandWhiteList();

  /** 是否启用两步验证 (TOTP)。 */
  boolean isTwoFactorEnabled();

  /** 验证器中显示的服务名。 */
  String getTwoFactorIssuer();

  /** TOTP 验证码位数。 */
  int getTwoFactorDigits();

  /** TOTP 时间步长 (秒)。 */
  int getTwoFactorPeriod();

  /** 启用两步验证时生成的恢复码数量。 */
  int getTwoFactorRecoveryCodeCount();

  /** 第二步验证允许的连续失败次数，达到后踢出。 */
  int getTwoFactorMaxAttempts();

  /** 失败计数的有效窗口 (秒)，超时后清零。 */
  int getTwoFactorAttemptWindow();

  /** 待验证状态超时 (秒)，0 表示沿用 settings.before-login.auto-kick。 */
  int getTwoFactorPendingTimeout();

  interface SpawnLocation {
    String getWorld();

    double getX();

    double getY();

    double getZ();

    float getYaw();

    float getPitch();
  }

  SpawnLocation getSpawnLocation();
}
