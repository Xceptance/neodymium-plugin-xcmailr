package util.xcmailr;

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

import org.aeonbits.owner.ConfigFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codeborne.selenide.Selenide;
import com.xceptance.neodymium.NeodymiumRunner;
import com.xceptance.neodymium.module.statement.browser.multibrowser.Browser;

import gherkin.deps.com.google.gson.JsonObject;
import gherkin.deps.com.google.gson.JsonParser;
import util.xcmailr.data.EmailAccount;
import util.xcmailr.data.SmtpAuthenticator;
import util.xcmailr.pageobjects.XcmailrLoginPage;
import util.xcmailr.pageobjects.XcmailrOverviewPage;
import util.xcmailr.util.Base64Decoder;

@Browser("Chrome_headless")
@RunWith(NeodymiumRunner.class)
public class XcMailrApiTest extends AbstractTest
{
    private final String xcmailrEmail = System.getenv("XCMAILR_EMAIL") != null ? System.getenv("XCMAILR_EMAIL") : "";

    private final String xcmailrPassword = System.getenv("XCMAILR_PASSWORD") != null ? System.getenv("XCMAILR_PASSWORD") : "";

    private final String tempEmail = "testTest1@varmail.de";

    private final int validMinutes = 3;

    private static final EmailAccount emailAccount = new EmailAccount("bulka.bolka@mail.ru", System.getenv("EMAIL_PASSWORD") != null ? System.getenv("EMAIL_PASSWORD")
                                                                                                                                     : "", "smtp.mail.ru", 465, true, false);

    @Before
    public void configureXcmailr()
    {
        final String apiToken = System.getenv("XCMAILR_TOKEN");
        if (apiToken != null)
        {
            properties2.put("xcmailr.apiToken", apiToken);
        }
        properties2.put("xcmailr.temporaryMailValidMinutes", String.valueOf(validMinutes));
        writeMapToPropertiesFile(properties2, tempConfigFile2);
        ConfigFactory.setProperty("xcmailr.temporaryConfigFile", "file:" + fileLocation);
    }

    @After
    public void deleteTempEmail()
    {
        new XcmailrLoginPage().login(xcmailrEmail, xcmailrPassword).openMailOverview().deleteTempEmail(tempEmail);
    }

    @Test
    public void testEmailCreated()
    {
        XcMailrApi.createTemporaryEmail(tempEmail);

        new XcmailrLoginPage().login(xcmailrEmail, xcmailrPassword).openMailOverview().validateEmailCreated(tempEmail);
    }

    @Test
    public void testEmailExpired()
    {
        final String xcmailrEmail = System.getenv("XCMAILR_EMAIL") != null ? System.getenv("XCMAILR_EMAIL") : "o.omelianchuk@xceptance.net";
        final String xcmailrPassword = System.getenv("XCMAILR_PASSWORD") != null ? System.getenv("XCMAILR_PASSWORD") : "parolXcmailr2020";
        XcMailrApi.createTemporaryEmail(tempEmail);
        XcmailrOverviewPage mailOverview = new XcmailrLoginPage().login(xcmailrEmail, xcmailrPassword).openMailOverview();
        mailOverview.validateEmailIsActive(tempEmail);
        Selenide.sleep((validMinutes + 1) * 60000);
        Selenide.refresh();
        Selenide.sleep(1000);
        mailOverview.validateEmailIsExpired(tempEmail);
    }

    @Test
    public void testRetrieveLastEmailBySubject() throws MessagingException
    {
        final String subject = "Test-1";
        final String textToSend = "Hi\nHow are you?)\nBye";
        XcMailrApi.createTemporaryEmail(tempEmail);
        send(emailAccount, tempEmail, subject, textToSend);

        JsonObject response = (JsonObject) new JsonParser().parse(XcMailrApi.retrieveLastEmailBySubject(tempEmail,
                                                                                                        subject))
                                                           .getAsJsonArray().get(0);
        Assert.assertEquals(response.get("mailAddress").toString().replaceAll("\"", ""), tempEmail);
        Assert.assertEquals(response.get("sender").toString().replaceAll("\"", ""), emailAccount.getEmail());
        Assert.assertEquals(response.get("subject").toString().replaceAll("\"", ""), subject);
        Assert.assertEquals(Base64Decoder.decode(response.get("htmlContent").toString().replaceAll("\"", "")), textToSend);
        Assert.assertEquals(Base64Decoder.decode(response.get("textContent").toString().replaceAll("\"", "")), textToSend);
    }

    /**
     * Method to send message
     * 
     * @param messageContainer
     *            message to send
     * @param text
     *            message text
     * @throws MessagingException
     */
    public void send(EmailAccount emailAccount, String recipent, String subject, String text) throws MessagingException
    {
        Properties smtpProps = new Properties();
        smtpProps.setProperty("mail.smtp.ssl.enable", Boolean.toString(emailAccount.isSsl()));
        smtpProps.setProperty("mail.smtp.tls.enable", Boolean.toString(emailAccount.isTls()));
        smtpProps.put("mail.smtp.host", emailAccount.getServer());
        smtpProps.put("mail.smtp.auth", "true");
        smtpProps.put("mail.stmp.port", emailAccount.getPort());
        smtpProps.setProperty("mail.user", emailAccount.getEmail());

        // create session
        SmtpAuthenticator smtpAuthenticator = new SmtpAuthenticator(emailAccount.getEmail(), emailAccount.getPassword());
        Session session = Session.getInstance(smtpProps, smtpAuthenticator);
        // Create the message part
        MimeMessage message = new MimeMessage(session);

        Address[] addresses =
        {
          new InternetAddress(recipent)
        };
        BodyPart messageBodyPartText = new MimeBodyPart();
        messageBodyPartText.setText(text);
        BodyPart messageBodyPartHtml = new MimeBodyPart();
        messageBodyPartHtml.setContent(text, "text/html; charset=utf-8");
        Multipart multipart = new MimeMultipart();
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
}
