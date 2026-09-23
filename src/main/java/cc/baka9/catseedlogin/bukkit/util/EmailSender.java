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

  /**
   * 同步发送邮件，发送失败时抛出异常由调用方处理。
   *
   * @throws EmailException 收件人为空、SMTP 端口非法或发送失败时抛出
   */
  public static void sendEmail(String receiveMailAccount, String subject, String content)
      throws EmailException {
    if (receiveMailAccount == null || receiveMailAccount.isEmpty()) {
      throw new EmailException("Receive mail account is empty");
    }
    HtmlEmail email = new HtmlEmail();
    email.setHostName(Config.EmailVerify.emailSmtpHost);
    email.setSmtpPort(parseSmtpPort());
    email.setAuthenticator(
        new DefaultAuthenticator(
            Config.EmailVerify.emailAccount, Config.EmailVerify.emailPassword));
    configureSecurity(email);
    email.setFrom(Config.EmailVerify.emailAccount, Config.EmailVerify.fromPersonal);
    email.setSubject(subject);
    email.setHtmlMsg(content);
    email.addTo(receiveMailAccount);
    email.setCharset("UTF-8");
    email.send();
  }

  private static int parseSmtpPort() throws EmailException {
    try {
      return Integer.parseInt(Config.EmailVerify.emailSmtpPort);
    } catch (NumberFormatException e) {
      throw new EmailException("Invalid SMTP port: " + Config.EmailVerify.emailSmtpPort, e);
    }
  }

  private static void configureSecurity(HtmlEmail email) {
    if (Config.EmailVerify.sslAuthVerify) {
      email.setSSLOnConnect(true);
      email.setSSLCheckServerIdentity(true);
    } else {
      email.setStartTLSEnabled(true);
    }
  }
}
