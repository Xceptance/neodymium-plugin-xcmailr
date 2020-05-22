package util.xcmailr;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.mail.MessagingException;

import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.util.Base64;
import org.apache.http.client.ClientProtocolException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codeborne.selenide.Selenide;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xceptance.neodymium.NeodymiumRunner;

import net.minidev.json.parser.ParseException;
import util.xcmailr.data.EmailAccount;
import util.xcmailr.util.Credentials;
import util.xcmailr.util.SendEmail;
import util.xcmailr.util.SendRequest;

@RunWith(NeodymiumRunner.class)
public class XcMailrApiTest extends AbstractTest
{
    protected final String xcmailrEmail = CREDENTIALS.xcmailrEmail();

    protected final String xcmailrPassword = CREDENTIALS.xcmailrPassword();

    protected static final Credentials CREDENTIALS = ConfigFactory.create(Credentials.class, System.getenv());

    protected final String tempEmail = "testTest1@varmail.net";

    @Before
    public void configureApiToken()
    {
        final String apiToken = System.getenv("XCMAILR_TOKEN");
        if (apiToken != null)
        {
            writeProperty("xcmailr.apiToken", apiToken);
        }
        writeProperty("xcmailr.temporaryMailValidMinutes", "3");
        SendRequest.login(xcmailrEmail, xcmailrPassword);
    }

    @After
    public void deleteTempEmail() throws ClientProtocolException, IOException
    {
        SendRequest.deleteTempEmail(tempEmail);
    }

    @Test
    public void testEmailCreated()
    {
        XcMailrApi.createTemporaryEmail(tempEmail);
        Assert.assertTrue(SendRequest.emailExists(tempEmail));
    }

    @Test
    public void testEmailExpired()
    {
        writeProperty("xcmailr.temporaryMailValidMinutes", "1");
        XcMailrApi.createTemporaryEmail(tempEmail);
        Assert.assertFalse(SendRequest.emailExpired(tempEmail));
        Selenide.sleep(120000);
        assertTrue(SendRequest.emailExpired(tempEmail));
    }

    @Test
    public void testRetrieveLastEmailBySubject() throws MessagingException, ParseException
    {
        final String subject = "Test";
        final String textToSend = "Hi\nHow are you?)\nBye";
        XcMailrApi.createTemporaryEmail(tempEmail);

        final EmailAccount emailAccount = new EmailAccount(CREDENTIALS.senderEmail(), CREDENTIALS.senderLogin(), CREDENTIALS.senderPassword(), CREDENTIALS.senderServer(), CREDENTIALS.senderPort(), false, true);

        SendEmail.send(emailAccount, tempEmail, subject, textToSend);

        String response = XcMailrApi.retrieveLastEmailBySubject(tempEmail, subject);
        JsonArray messagesArray = new JsonParser().parse(response).getAsJsonArray();
        JsonObject message = messagesArray.get(0).getAsJsonObject();

        Assert.assertEquals(message.get("mailAddress").getAsString().replaceAll("\"", ""), tempEmail);
        Assert.assertEquals(message.get("sender").getAsString().replaceAll("\"", ""), emailAccount.getEmail());
        Assert.assertEquals(message.get("subject").getAsString().replaceAll("\"", ""), subject);
        Assert.assertEquals(decode(message.get("htmlContent").getAsString()).replaceAll("\"", ""), textToSend);
        Assert.assertEquals(decode(message.get("textContent").getAsString()).replaceAll("\"", ""), textToSend);
    }

    private String decode(String text)
    {
        return new String(Base64.decode(text)).replaceAll(String.valueOf((char) 13), "");
    }
}