package util.xcmailr;

import javax.mail.MessagingException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minidev.json.parser.ParseException;
import util.xcmailr.data.EmailAccount;
import util.xcmailr.util.SendEmail;

public class XcMailRetrieveEmailsTest extends AbstractXcMailrApiTest
{
    private final String subject = "Test";

    private final String textToSend = "Hi\nHow are you?)\nBye";

    private final EmailAccount emailAccount = new EmailAccount(CREDENTIALS.senderEmail(), CREDENTIALS.senderLogin(), CREDENTIALS.senderPassword(), CREDENTIALS.senderServer(), CREDENTIALS.senderPort(), false, true);

    @Before
    public void createTempEmailAndSendMessage()
    {
        XcMailrApi.createTemporaryEmail(tempEmail);
        SendEmail.send(emailAccount, tempEmail, subject, textToSend);
    }

    @Test
    public void testRetrieveLastEmailBySubject() throws MessagingException, ParseException
    {
        String response = XcMailrApi.retrieveLastEmailBySubject(tempEmail, subject);
        JsonArray messagesArray = new JsonParser().parse(response).getAsJsonArray();
        JsonObject message = messagesArray.get(0).getAsJsonObject();

        Assert.assertEquals(message.get("mailAddress").getAsString().replaceAll("\"", ""), tempEmail);
        Assert.assertEquals(message.get("sender").getAsString().replaceAll("\"", ""), emailAccount.getEmail());
        Assert.assertEquals(message.get("subject").getAsString().replaceAll("\"", ""), subject);
        Assert.assertEquals(decode(message.get("htmlContent").getAsString()).replaceAll("\"", ""), textToSend);
        Assert.assertEquals(decode(message.get("textContent").getAsString()).replaceAll("\"", ""), textToSend);
    }

    @Test
    public void testRetrieveLastEmailBySender() throws MessagingException, ParseException
    {
        String response = XcMailrApi.retrieveLastEmailBySender(tempEmail, emailAccount.getEmail());
        JsonArray messagesArray = new JsonParser().parse(response).getAsJsonArray();
        JsonObject message = messagesArray.get(0).getAsJsonObject();

        Assert.assertEquals(message.get("mailAddress").getAsString().replaceAll("\"", ""), tempEmail);
        Assert.assertEquals(message.get("sender").getAsString().replaceAll("\"", ""), emailAccount.getEmail());
        Assert.assertEquals(message.get("subject").getAsString().replaceAll("\"", ""), subject);
        Assert.assertEquals(decode(message.get("htmlContent").getAsString()).replaceAll("\"", ""), textToSend);
        Assert.assertEquals(decode(message.get("textContent").getAsString()).replaceAll("\"", ""), textToSend);
    }

    @Test
    public void fetchLastEmail() throws MessagingException, ParseException
    {
        String response = XcMailrApi.fetchEmails(tempEmail, null, null, null, null, null, true);
        JsonArray messagesArray = new JsonParser().parse(response).getAsJsonArray();
        JsonObject message = messagesArray.get(0).getAsJsonObject();

        Assert.assertEquals(message.get("mailAddress").getAsString().replaceAll("\"", ""), tempEmail);
        Assert.assertEquals(message.get("sender").getAsString().replaceAll("\"", ""), emailAccount.getEmail());
        Assert.assertEquals(message.get("subject").getAsString().replaceAll("\"", ""), subject);
        Assert.assertEquals(decode(message.get("htmlContent").getAsString()).replaceAll("\"", ""), textToSend);
        Assert.assertEquals(decode(message.get("textContent").getAsString()).replaceAll("\"", ""), textToSend);
    }

    @Test
    public void fetchLastEmailByTextContent() throws MessagingException, ParseException
    {
        String response = XcMailrApi.fetchEmails(tempEmail, null, null, textToSend, null, null, true);
        JsonArray messagesArray = new JsonParser().parse(response).getAsJsonArray();
        JsonObject message = messagesArray.get(0).getAsJsonObject();

        Assert.assertEquals(message.get("mailAddress").getAsString().replaceAll("\"", ""), tempEmail);
        Assert.assertEquals(message.get("sender").getAsString().replaceAll("\"", ""), emailAccount.getEmail());
        Assert.assertEquals(message.get("subject").getAsString().replaceAll("\"", ""), subject);
        Assert.assertEquals(decode(message.get("htmlContent").getAsString()).replaceAll("\"", ""), textToSend);
        Assert.assertEquals(decode(message.get("textContent").getAsString()).replaceAll("\"", ""), textToSend);
    }

    @Test
    public void fetchLastEmailByHtmlContent() throws MessagingException, ParseException
    {
        String response = XcMailrApi.fetchEmails(tempEmail, null, null, null, textToSend, null, true);
        JsonArray messagesArray = new JsonParser().parse(response).getAsJsonArray();
        JsonObject message = messagesArray.get(0).getAsJsonObject();

        Assert.assertEquals(message.get("mailAddress").getAsString().replaceAll("\"", ""), tempEmail);
        Assert.assertEquals(message.get("sender").getAsString().replaceAll("\"", ""), emailAccount.getEmail());
        Assert.assertEquals(message.get("subject").getAsString().replaceAll("\"", ""), subject);
        Assert.assertEquals(decode(message.get("htmlContent").getAsString()).replaceAll("\"", ""), textToSend);
        Assert.assertEquals(decode(message.get("textContent").getAsString()).replaceAll("\"", ""), textToSend);
    }

    @Test
    public void fetchEmailsFromTempEmail() throws MessagingException, ParseException
    {
        SendEmail.send(emailAccount, tempEmail, subject, textToSend);

        String response = XcMailrApi.fetchEmails(tempEmail, null, null, null, null, null, false);
        JsonArray messagesArray = new JsonParser().parse(response).getAsJsonArray();
        Assert.assertEquals(messagesArray.size(), 2);

        for (int i = 0; i < 2; i++)
        {
            JsonObject message = messagesArray.get(i).getAsJsonObject();

            Assert.assertEquals(message.get("mailAddress").getAsString().replaceAll("\"", ""), tempEmail);
            Assert.assertEquals(message.get("sender").getAsString().replaceAll("\"", ""), emailAccount.getEmail());
            Assert.assertEquals(message.get("subject").getAsString().replaceAll("\"", ""), subject);
            Assert.assertEquals(decode(message.get("htmlContent").getAsString()).replaceAll("\"", ""), textToSend);
            Assert.assertEquals(decode(message.get("textContent").getAsString()).replaceAll("\"", ""), textToSend);
        }
    }
}
