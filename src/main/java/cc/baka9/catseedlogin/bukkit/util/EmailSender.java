package cc.baka9.catseedlogin.bukkit.util;

import cc.baka9.catseedlogin.bukkit.PluginContext;
import cc.baka9.catseedlogin.bukkit.config.BukkitConfigManager;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

public final class EmailSender {
  private EmailSender() {}

  public static void sendEmail(String receiveMailAccount, String subject, String content) {
    if (receiveMailAccount == null || receiveMailAccount.isEmpty()) {
      return;
    }
    BukkitConfigManager cm = PluginContext.getConfigManager();
    HtmlEmail email = new HtmlEmail();
    email.setHostName(cm.getEmailSmtpHost());
    try {
      email.setSmtpPort(Integer.parseInt(cm.getEmailSmtpPort()));
    } catch (NumberFormatException e) {
      return;
    }
    email.setAuthenticator(
        new DefaultAuthenticator(cm.getEmailAccount(), cm.getEmailPassword()));
    configureSecurity(email, cm);
    try {
      email.setFrom(cm.getEmailAccount(), cm.getFromPersonal());
      email.setSubject(subject);
      email.setHtmlMsg(content);
      email.addTo(receiveMailAccount);
      email.setCharset("UTF-8");
      email.send();
    } catch (EmailException e) {
      e.printStackTrace();
    }
  }

  private static void configureSecurity(HtmlEmail email, BukkitConfigManager cm) {
    if (cm.isSSLAuthVerify()) {
      email.setSSLOnConnect(true);
      email.setSSLCheckServerIdentity(true);
    } else {
      email.setStartTLSEnabled(true);
    }
  }
}
