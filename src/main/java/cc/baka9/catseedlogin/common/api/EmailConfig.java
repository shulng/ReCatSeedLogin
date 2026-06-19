package cc.baka9.catseedlogin.common.api;

public interface EmailConfig {

    boolean isEmailEnable();
    String getEmailAccount();
    String getEmailPassword();
    String getEmailSmtpHost();
    String getEmailSmtpPort();
    boolean isSSLAuthVerify();
    String getFromPersonal();
}
