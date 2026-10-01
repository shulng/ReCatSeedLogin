package cc.baka9.catseedlogin.common.email;

import cc.baka9.catseedlogin.common.api.EmailConfig;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

/** 邮件发送器，依赖 {@link EmailConfig} 提供的 SMTP 配置，与具体平台无关。 */
public final class EmailSender {

  private EmailSender() {}

  public static void send(
      EmailConfig config, String receiveMailAccount, String subject, String content) {
    if (receiveMailAccount == null || receiveMailAccount.isEmpty()) {
      return;
    }
    HtmlEmail email = new HtmlEmail();
    email.setHostName(config.getEmailSmtpHost());
    try {
      email.setSmtpPort(Integer.parseInt(config.getEmailSmtpPort()));
    } catch (NumberFormatException e) {
      return;
    }
    email.setAuthenticator(
        new DefaultAuthenticator(config.getEmailAccount(), config.getEmailPassword()));
    configureSecurity(email, config);
    try {
      email.setFrom(config.getEmailAccount(), config.getFromPersonal());
      email.setSubject(subject);
      email.setHtmlMsg(content);
      email.addTo(receiveMailAccount);
      email.setCharset("UTF-8");
      email.send();
    } catch (EmailException e) {
      e.printStackTrace();
    }
  }

  private static void configureSecurity(HtmlEmail email, EmailConfig config) {
    if (config.isSSLAuthVerify()) {
      email.setSSLOnConnect(true);
      email.setSSLCheckServerIdentity(true);
    } else {
      email.setStartTLSEnabled(true);
    }
  }
}
