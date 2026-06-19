package cc.baka9.catseedlogin.common.api;

import cc.baka9.catseedlogin.common.config.Configuration;

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
