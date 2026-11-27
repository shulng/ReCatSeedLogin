package cc.baka9.catseedlogin.bukkit.util

import cc.baka9.catseedlogin.bukkit.Config
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.EmailException
import org.apache.commons.mail.HtmlEmail

object EmailSender {
    @JvmStatic fun sendEmail(receiveMailAccount: String?, subject: String, content: String) {
        if (receiveMailAccount.isNullOrEmpty()) return
        val email = HtmlEmail()
        email.setHostName(Config.EmailVerify.EmailSmtpHost)
        try { email.setSmtpPort(Config.EmailVerify.EmailSmtpPort.toInt()) } catch (_: NumberFormatException) { return }
        email.setAuthenticator(DefaultAuthenticator(Config.EmailVerify.EmailAccount, Config.EmailVerify.EmailPassword))
        configureSecurity(email)
        try {
            email.setFrom(Config.EmailVerify.EmailAccount, Config.EmailVerify.FromPersonal)
            email.setSubject(subject)
            email.setHtmlMsg(content)
            email.addTo(receiveMailAccount)
            email.setCharset("UTF-8")
            email.send()
        } catch (e: EmailException) { e.printStackTrace() }
    }
    private fun configureSecurity(email: HtmlEmail) {
        if (Config.EmailVerify.SSLAuthVerify) {
            email.setSSLOnConnect(true)
            email.setSSLCheckServerIdentity(true)
        } else email.setStartTLSEnabled(true)
    }
}
