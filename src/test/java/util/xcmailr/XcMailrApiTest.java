package util.xcmailr;

import java.io.IOException;
import java.util.Map;
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
import org.apache.http.client.ClientProtocolException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codeborne.selenide.Selenide;
import com.google.common.collect.Maps;
import com.xceptance.neodymium.NeodymiumRunner;
import com.xceptance.neodymium.module.statement.browser.multibrowser.Browser;
import com.xceptance.neodymium.module.statement.browser.multibrowser.SuppressBrowsers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import util.xcmailr.data.EmailAccount;
import util.xcmailr.data.SmtpAuthenticator;
import util.xcmailr.pageobjects.XcmailrLoginPage;
import util.xcmailr.pageobjects.XcmailrOverviewPage;
import util.xcmailr.util.Base64Decoder;
import util.xcmailr.util.SendRequest;

@Browser("Chrome_headless")
@RunWith(NeodymiumRunner.class)
// TODO make runnable locally without the need to write in the environment
// FIXME Get the environment once and perform lookup on the map to set values
public class XcMailrApiTest extends AbstractTest
{
    // FIXME unused -> remove
    Map<String, String> headers = Maps.newHashMap();

    // FIXME unused -> remove
    Map<String, String> formParams = Maps.newHashMap();

    private final String xcmailrEmail = System.getenv("XCMAILR_EMAIL") != null ? System.getenv("XCMAILR_EMAIL") : "";

    private final String xcmailrPassword = System.getenv("XCMAILR_PASSWORD") != null ? System.getenv("XCMAILR_PASSWORD") : "";

    private final String tempEmail = "testTest@varmail.net";

    private final int validMinutes = 3;

    // TODO only used once, move in method?
    private static final EmailAccount emailAccount = new EmailAccount(System.getenv("EMAIL") != null ? System.getenv("EMAIL") : "",
                                                                      System.getenv("EMAIL_LOGIN") != null ? System.getenv("EMAIL_LOGIN")
                                                                                                           : "",
                                                                      System.getenv("EMAIL_PASSWORD") != null ? System.getenv("EMAIL_PASSWORD")
                                                                                                              : "",
                                                                      System.getenv("EMAIL_SERVER") != null ? System.getenv("EMAIL_SERVER")
                                                                                                            : "",
                                                                      25, false, true);

    @Before
    public void configureApiToken()
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
    public void deleteTempEmail() throws ClientProtocolException, IOException
    {
        SendRequest.login(xcmailrEmail, xcmailrPassword);
        SendRequest.deleteTempEmail(tempEmail);
    }

    @Test
    public void testEmailCreated()
    {
        XcMailrApi.createTemporaryEmail(tempEmail);

        new XcmailrLoginPage().login(xcmailrEmail, xcmailrPassword).openMailOverview().validateEmailCreated(tempEmail);
    }

    @Test
    // TODO use minimal validity period
    public void testEmailExpired()
    {
        XcMailrApi.createTemporaryEmail(tempEmail);
        final XcmailrOverviewPage mailOverview = new XcmailrLoginPage().login(xcmailrEmail, xcmailrPassword).openMailOverview();
        mailOverview.validateEmailIsActive(tempEmail);
        // FIXME remove additional minute of wait. We already know that the email is active, so the timer is already on
        Selenide.sleep((validMinutes + 1) * 60000);
        Selenide.refresh();
        Selenide.sleep(1000);
        mailOverview.validateEmailIsExpired(tempEmail);
    }

    @SuppressBrowsers
    @Test
    public void testRetrieveLastEmailBySubject() throws MessagingException, ParseException
    {
        final String subject = "Test";
        final String textToSend = "Hi\nHow are you?)\nBye";
        XcMailrApi.createTemporaryEmail(tempEmail);
        send(emailAccount, tempEmail, subject, textToSend);
        final JSONObject response = (JSONObject) ((JSONArray) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(XcMailrApi.retrieveLastEmailBySubject(tempEmail,
                                                                                                                                                      subject))).get(0);
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
    // TODO this should be a static method
    public void send(EmailAccount emailAccount, String recipent, String subject, String text)
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
                    new InternetAddress(recipent)
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
            e.printStackTrace();
        }
    }
}
