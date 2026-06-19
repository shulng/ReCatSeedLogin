package cc.baka9.catseedlogin.common.api;

public interface BungeeCordConfig {

    boolean isEnable();
    String getProxyHost();
    int getProxyPort();
    String getAuthKey();
    String getLoginServerName();
}
