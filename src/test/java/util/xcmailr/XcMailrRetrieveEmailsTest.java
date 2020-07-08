package util.xcmailr;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.codeborne.selenide.Selenide;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import util.xcmailr.data.EmailAccount;
import util.xcmailr.util.SendEmail;

public class XcMailrRetrieveEmailsTest extends AbstractXcMailrApiTest
{
    private final String subject = "Test";

    private final String textToSend = "Hi\nHow are you?)\nBye";

    private final EmailAccount emailAccount = new EmailAccount(CREDENTIALS.smtpServerEmail(), CREDENTIALS.smtpServerLogin(),
                                                               CREDENTIALS.smtpServerPassword(), CREDENTIALS.smtpServerHost(),
                                                               CREDENTIALS.smtpServerPort(), false, true);

    @Before
    public void createTempEmailAndSendMessage() throws IOException
    {
        XcMailrApi.createTemporaryEmail(tempEmail);
        SendEmail.send(emailAccount, tempEmail, subject, textToSend);
    }

    /**
     * test that the method <code>XcMailrApi.retrieveLastEmailBySubject</code> works correct</br>
     * in other words, that it's possible to fetch last received e-mail with a specific subject
     */
    @Test
    public void testRetrieveLastEmailBySubject()
    {
        String response = XcMailrApi.retrieveLastEmailBySubject(tempEmail, subject);
        validateMessage(parseMessage(response));
    }

    /**
     * test that the method <code>XcMailrApi.retrieveLastEmailBySender</code> works correct</br>
     * in other words, that it's possible to fetch last received e-mail with a specific sender
     */
    @Test
    public void testRetrieveLastEmailBySender()
    {
        String response = XcMailrApi.retrieveLastEmailBySender(tempEmail, emailAccount.getEmail());
        validateMessage(parseMessage(response));
    }

    /**
     * test that it's possible to fetch the last received e-mail
     */
    @Test
    public void fetchLastEmail()
    {
        // fetch last received e-mail
        String response = XcMailrApi.fetchEmails(tempEmail, null, null, null, null, null, true);
        validateMessage(parseMessage(response));
    }

    /**
     * test that it's possible to fetch last received e-mail with a specific plain text
     */
    @Test
    public void fetchLastEmailByTextContent()
    {
        String textToRetrieve = textToSend.replaceAll("\\?", "\\\\?").replaceAll("\\)", "\\\\)").replaceAll("\\n", "\\\\r\\\\n");

        // fetch last received e-mail with specified plain text
        String response = XcMailrApi.fetchEmails(tempEmail, null, null, textToRetrieve, null, null, true);
        validateMessage(parseMessage(response));
    }

    /**
     * test that it's possible to fetch last received e-mail with a specific HTML text
     */
    @Test
    public void fetchLastEmailByHtmlContent()
    {
        String textToRetrieve = textToSend.replaceAll("\\?", "\\\\?").replaceAll("\\)", "\\\\)").replaceAll("\\n", "\\\\r\\\\n");

        // fetch last received e-mail with specified HTML text
        String response = XcMailrApi.fetchEmails(tempEmail, null, null, null, textToRetrieve, null, true);
        validateMessage(parseMessage(response));
    }

    /**
     * test that it's possible to fetch all received e-mails
     */
    @Test
    public void fetchEmailsFromTempEmail()
    {
        SendEmail.send(emailAccount, tempEmail, subject, textToSend);
        Selenide.sleep(1000);
        // fetch all received e-mails
        String response = XcMailrApi.fetchEmails(tempEmail, null, null, null, null, null, false);
        JsonArray messagesArray = new JsonParser().parse(response).getAsJsonArray();
        Assert.assertEquals(2, messagesArray.size());

        for (int i = 0; i < 2; i++)
        {
            JsonObject message = messagesArray.get(i).getAsJsonObject();

            validateMessage(message);
        }
    }

    private JsonObject parseMessage(String response)
    {
        JsonArray messagesArray = new JsonParser().parse(response).getAsJsonArray();
        return messagesArray.get(0).getAsJsonObject();
    }

    private void validateMessage(JsonObject message)
    {
        Assert.assertEquals(message.get("mailAddress").getAsString().replaceAll("\"", ""), tempEmail);
        Assert.assertEquals(message.get("sender").getAsString().replaceAll("\"", ""), emailAccount.getEmail());
        Assert.assertEquals(message.get("subject").getAsString().replaceAll("\"", ""), subject);
        Assert.assertEquals(decodeAndNormalize(message.get("htmlContent").getAsString()).replaceAll("\"", ""), textToSend);
        Assert.assertEquals(decodeAndNormalize(message.get("textContent").getAsString()).replaceAll("\"", ""), textToSend);
    }
}
