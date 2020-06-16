package util.xcmailr;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import util.xcmailr.data.EmailAccount;
import util.xcmailr.util.SendEmail;
import util.xcmailr.util.SendRequest;

public class XcMailrApiTest extends AbstractXcMailrApiTest
{
    @Test
    public void testEmailCreated() throws IOException
    {
        XcMailrApi.createTemporaryEmail(tempEmail);
        Assert.assertTrue(SendRequest.emailExists(tempEmail));
    }

    @Test
    public void testEmailExpired() throws IOException
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
    public void testRetrieveLastEmailBySubject() throws MessagingException, IOException
    {
        final String subject = "Test";
        final String textToSend = "Hi\nHow are you?)\nBye";
        XcMailrApi.createTemporaryEmail(tempEmail);

        final EmailAccount emailAccount = new EmailAccount(CREDENTIALS.smtpServerEmail(), CREDENTIALS.smtpServerLogin(),
                                                           CREDENTIALS.smtpServerPassword(), CREDENTIALS.smtpServerHost(),
                                                           CREDENTIALS.smtpServerPort(), false, true);

        SendEmail.send(emailAccount, tempEmail, subject, textToSend);

        String response = XcMailrApi.retrieveLastEmailBySubject(tempEmail, subject);
        JsonObject message = new JsonParser().parse(response).getAsJsonArray().get(0).getAsJsonObject();

        Assert.assertEquals(message.get("mailAddress").getAsString().replaceAll("\"", ""), tempEmail);
        Assert.assertEquals(message.get("sender").getAsString().replaceAll("\"", ""), emailAccount.getEmail());
        Assert.assertEquals(message.get("subject").getAsString().replaceAll("\"", ""), subject);
        Assert.assertEquals(decodeAndNormalize(message.get("htmlContent").getAsString()).replaceAll("\"", ""), textToSend);
        Assert.assertEquals(decodeAndNormalize(message.get("textContent").getAsString()).replaceAll("\"", ""), textToSend);
    }
}
