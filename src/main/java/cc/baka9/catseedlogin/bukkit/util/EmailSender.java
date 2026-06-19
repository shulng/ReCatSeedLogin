package cc.baka9.catseedlogin.bukkit.util;

import cc.baka9.catseedlogin.bukkit.Config;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.EmailException;

public final class EmailSender {
    private EmailSender() {}

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
        email.setAuthenticator(new DefaultAuthenticator(Config.EmailVerify.EmailAccount, Config.EmailVerify.EmailPassword));
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