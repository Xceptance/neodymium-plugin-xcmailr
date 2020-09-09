package com.xceptance.neodymium.plugin.xcmailr.util;

import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
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

import com.xceptance.neodymium.plugin.xcmailr.data.EmailAccount;
import com.xceptance.neodymium.plugin.xcmailr.data.SmtpAuthenticator;

public class SendEmail
{
    /**
     * Sends an e-mail from the specified <code>emailAccount</code> to the specified <code>recipient</code>.
     * 
     * @param emailAccount
     *            an {@link EmailAccount} object with configuration of SMTP server and credentials for it
     * @param recipient
     *            e-mail address of recipient
     * @param subject
     *            subject of the e-mail
     * @param text
     *            text to send
     */
    public static void send(EmailAccount emailAccount, String recipient, String subject, String text, File fileToSend)
    {
        final Properties smtpProps = new Properties();
        smtpProps.setProperty("mail.smtp.ssl.enable", Boolean.toString(emailAccount.isSsl()));
        smtpProps.setProperty("mail.smtp.tls.enable", Boolean.toString(emailAccount.isTls()));
        smtpProps.put("mail.smtp.host", emailAccount.getServer());
        smtpProps.put("mail.smtp.auth", Boolean.toString(emailAccount.getPassword() != null));
        smtpProps.put("mail.smtp.port", emailAccount.getPort());
        smtpProps.setProperty("mail.user", emailAccount.getLogin());

        // create session
        final Session session;
        if (emailAccount.getPassword() != null)
        {
            final SmtpAuthenticator smtpAuthenticator = new SmtpAuthenticator(emailAccount.getLogin(), emailAccount.getPassword());
            session = Session.getInstance(smtpProps, smtpAuthenticator);
        }
        else
        {
            session = Session.getDefaultInstance(smtpProps);
        }

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

            final BodyPart messageBodyPartAttachment = new MimeBodyPart();

            final DataSource source = new FileDataSource(fileToSend);
            messageBodyPartAttachment.setDataHandler(new DataHandler(source));
            messageBodyPartAttachment.setFileName(fileToSend.getName());

            final Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPartText);
            multipart.addBodyPart(messageBodyPartHtml);
            multipart.addBodyPart(messageBodyPartAttachment);

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

    /**
     * Sends an e-mail from the specified <code>sender</code> to the specified <code>recipient</code>. using localhost
     * server
     * 
     * @param sender
     *            an address of sender (can be any e-mail)
     * @param recipient
     *            e-mail address of recipient
     * @param subject
     *            subject of the e-mail
     * @param text
     *            text to send
     */
    public static void sendViaLocalNet(String sender, String recipient, String subject, String text, File file)
    {
        EmailAccount localhostEmail = new EmailAccount(sender, null, "localhost", 25000, false, false);
        send(localhostEmail, recipient, subject, text, file);
    }
}
