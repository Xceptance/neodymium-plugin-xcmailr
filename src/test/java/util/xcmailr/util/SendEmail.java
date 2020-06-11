package util.xcmailr.util;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import util.xcmailr.data.EmailAccount;
import util.xcmailr.data.SmtpAuthenticator;

public class SendEmail
{
    /**
     * Sends an e-mail from the specified <code>emailAccount</code> to the specified <code>recipient</code>.
     * 
     * @param emailAccount
     *            is an <code>EmailAccount</code> object containing configuration of SMTP server and credentials for it
     * @param recipient
     *            e-mail address of recipient
     * @param subject
     *            subject of the e-mail
     * @param text
     *            text to send
     */
    public static void send(EmailAccount emailAccount, String recipient, String subject, String text)
    {
        final Properties smtpProps = new Properties();
        smtpProps.setProperty("mail.smtp.ssl.enable", Boolean.toString(emailAccount.isSsl()));
        smtpProps.setProperty("mail.smtp.tls.enable", Boolean.toString(emailAccount.isTls()));
        smtpProps.put("mail.smtp.host", emailAccount.getServer());
        smtpProps.put("mail.smtp.auth", "true");
        smtpProps.put("mail.stmp.port", emailAccount.getPort());
        smtpProps.setProperty("mail.user", emailAccount.getLogin());

        // create session
        final SmtpAuthenticator smtpAuthenticator = new SmtpAuthenticator(emailAccount.getLogin(), emailAccount.getPassword());
        final Session session = Session.getInstance(smtpProps, smtpAuthenticator);

        // Create the message part
        final MimeMessage message = new MimeMessage(session);

        try
        {

            final Address[] addresses =
                {
                    new InternetAddress(recipient)
                };
            final BodyPart messageBodyPartText = new MimeBodyPart();
            messageBodyPartText.setText(text);
            final BodyPart messageBodyPartHtml = new MimeBodyPart();
            messageBodyPartHtml.setContent(text, "text/html; charset=utf-8");
            final Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPartText);
            multipart.addBodyPart(messageBodyPartHtml);

            message.setRecipients(Message.RecipientType.TO, addresses);
            message.setSubject(subject);
            message.setFrom(new InternetAddress(emailAccount.getEmail()));
            message.setContent(multipart);

            // Send message
            Transport.send(message);
            session.getTransport().close();
        }
        catch (final MessagingException e)
        {
            throw new RuntimeException("Failed to send e-mail via SMTP server", e);
        }
    }
}
