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
