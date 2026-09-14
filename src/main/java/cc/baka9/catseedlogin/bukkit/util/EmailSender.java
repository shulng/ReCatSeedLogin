package cc.baka9.catseedlogin.bukkit.util;

import cc.baka9.catseedlogin.bukkit.config.Config;
import cc.baka9.catseedlogin.bukkit.scheduler.CatScheduler;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

public final class EmailSender {
  private EmailSender() {}

  /**
   * 异步发送邮件，并在主线程执行成功/失败回调。
   *
   * @param onSuccess 发送成功后的回调（主线程执行）
   * @param onFailure 发送失败后的回调（主线程执行）
   */
  public static void sendEmailAsync(
      String receiveMailAccount, String subject, String content, Runnable onSuccess,
      Runnable onFailure) {
    CatScheduler.runTaskAsync(
        () -> {
          try {
            sendEmail(receiveMailAccount, subject, content);
            CatScheduler.runTask(onSuccess);
          } catch (Exception e) {
            CatScheduler.runTask(onFailure);
            e.printStackTrace();
          }
        });
  }

  public static void sendEmail(String receiveMailAccount, String subject, String content) {
    if (receiveMailAccount == null || receiveMailAccount.isEmpty()) {
      return;
    }
    HtmlEmail email = new HtmlEmail();
    email.setHostName(Config.EmailVerify.EmailSmtpHost);
    try {
      email.setSmtpPort(Integer.parseInt(Config.EmailVerify.EmailSmtpPort));
    } catch (NumberFormatException e) {
      return;
    }
    email.setAuthenticator(
        new DefaultAuthenticator(
            Config.EmailVerify.EmailAccount, Config.EmailVerify.EmailPassword));
    configureSecurity(email);
    try {
      email.setFrom(Config.EmailVerify.EmailAccount, Config.EmailVerify.FromPersonal);
      email.setSubject(subject);
      email.setHtmlMsg(content);
      email.addTo(receiveMailAccount);
      email.setCharset("UTF-8");
      email.send();
    } catch (EmailException e) {
      e.printStackTrace();
    }
  }

  private static void configureSecurity(HtmlEmail email) {
    if (Config.EmailVerify.SSLAuthVerify) {
      email.setSSLOnConnect(true);
      email.setSSLCheckServerIdentity(true);
    } else {
      email.setStartTLSEnabled(true);
    }
  }
}
