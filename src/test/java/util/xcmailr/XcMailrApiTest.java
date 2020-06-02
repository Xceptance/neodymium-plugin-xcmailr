package util.xcmailr;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;

import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.util.Base64;
import org.apache.http.client.ClientProtocolException;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xceptance.neodymium.NeodymiumRunner;
import com.xceptance.neodymium.util.DataUtils;

import net.minidev.json.parser.ParseException;
import util.xcmailr.data.EmailAccount;
import util.xcmailr.util.Credentials;
import util.xcmailr.util.SendEmail;
import util.xcmailr.util.SendRequest;

@RunWith(NeodymiumRunner.class)
public class XcMailrApiTest extends AbstractTest
{
    protected static final Credentials CREDENTIALS = ConfigFactory.create(Credentials.class, System.getenv());

    protected static final String validMinutes = "1";

    protected static String tempEmail;

    @BeforeClass
    public static void configureApiToken() throws ClientProtocolException, IOException
    {
        tempEmail = DataUtils.randomEmail();
        final String xcmailrEmail = CREDENTIALS.xcmailrEmail();

        final String xcmailrPassword = CREDENTIALS.xcmailrPassword();

        final String apiToken = System.getenv("XCMAILR_TOKEN");
        if (apiToken != null)
        {
            properties.put("xcmailr.apiToken", apiToken);
        }
        properties.put("xcmailr.temporaryMailValidMinutes", validMinutes);
        writeProperty();
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
        String response = XcMailrApi.createTemporaryEmail(tempEmail);

        Pattern validMinutesPatters = Pattern.compile("<div class=\"email-validity-minutes\">(\\d)</div>");
        Matcher matcher = validMinutesPatters.matcher(response);
        while (matcher.find())
        {
            Assert.assertEquals(validMinutes, matcher.group(1));
        }
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
        JsonObject message = new JsonParser().parse(response).getAsJsonObject();

        Assert.assertEquals(message.get("mailAddress").getAsString().replaceAll("\"", ""), tempEmail);
        Assert.assertEquals(message.get("sender").getAsString().replaceAll("\"", ""), emailAccount.getEmail());
        Assert.assertEquals(message.get("subject").getAsString().replaceAll("\"", ""), subject);
        Assert.assertEquals(decodeAndNormalize(message.get("htmlContent").getAsString()).replaceAll("\"", ""), textToSend);
        Assert.assertEquals(decodeAndNormalize(message.get("textContent").getAsString()).replaceAll("\"", ""), textToSend);
    }

    private String decodeAndNormalize(String text)
    {
        return new String(Base64.decode(text)).replaceAll(String.valueOf((char) 13), "");
    }
}
