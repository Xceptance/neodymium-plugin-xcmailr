package util.xcmailr;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.codeborne.selenide.Selenide;

import util.xcmailr.data.EmailAccount;
import util.xcmailr.util.SendEmail;
import xcmailr.client.Mail;

public class XcMailrRetrieveEmailsTest extends AbstractXcMailrApiTest
{
    private final String subject = "Test";

    private final String textToSend = "Hi\nHow are you?)\nBye";

    private final EmailAccount emailAccount = new EmailAccount(CREDENTIALS.smtpServerEmail(), CREDENTIALS.smtpServerLogin(), CREDENTIALS.smtpServerPassword(), CREDENTIALS.smtpServerHost(), CREDENTIALS.smtpServerPort(), false, true);

    @Before
    public void createTempEmailAndSendMessage() throws IOException, URISyntaxException, InterruptedException
    {
        XcMailrApi.createTemporaryEmail(emailUnderTest, false);

        SendEmail.send(emailAccount, emailUnderTest, subject, textToSend);
    }

    /**
     * test that the method <code>XcMailrApi.retrieveLastEmailBySubject</code> works correct</br>
     * in other words, that it's possible to fetch last received e-mail with a specific subject
     */
    @Test
    public void testRetrieveLastEmailBySubject()
    {
        Mail mail = XcMailrApi.retrieveLastEmailBySubject(emailUnderTest, subject).get(0);
        validateMessage(mail);
    }

    /**
     * test that the method <code>XcMailrApi.retrieveLastEmailBySender</code> works correct</br>
     * in other words, that it's possible to fetch last received e-mail with a specific sender
     */
    @Test
    public void testRetrieveLastEmailBySender()
    {
        Mail mail = XcMailrApi.retrieveLastEmailBySender(emailUnderTest, emailAccount.getEmail()).get(0);
        validateMessage(mail);
    }

    /**
     * test that it's possible to fetch the last received e-mail
     */
    @Test
    public void fetchLastEmail()
    {
        // fetch last received e-mail
        Mail mail = XcMailrApi.fetchEmails(emailUnderTest, null, null, null, null, true).get(0);
        validateMessage(mail);
    }

    /**
     * test that it's possible to fetch last received e-mail with a specific plain text
     */
    @Test
    public void fetchLastEmailByTextContent()
    {
        String textToRetrieve = textToSend.replaceAll("\\?", "\\\\?").replaceAll("\\)", "\\\\)").replaceAll("\\n", "\\\\r\\\\n");

        // fetch last received e-mail with specified plain text
        Mail mail = XcMailrApi.fetchEmails(emailUnderTest, null, null, textToRetrieve, null, true).get(0);
        validateMessage(mail);
    }

    /**
     * test that it's possible to fetch last received e-mail with a specific HTML text
     */
    @Test
    public void fetchLastEmailByHtmlContent()
    {
        String textToRetrieve = textToSend.replaceAll("\\?", "\\\\?").replaceAll("\\)", "\\\\)").replaceAll("\\n", "\\\\r\\\\n");

        // fetch last received e-mail with specified HTML text
        Mail mail = XcMailrApi.fetchEmails(emailUnderTest, null, null, null, textToRetrieve, true).get(0);
        validateMessage(mail);
    }

    /**
     * assert that the validity of the pattern for text content will be checked before to make a request on server
     */
    @Test
    public void fetchLastEmailByTextContentWithInvalidPattern()
    {
        Assert.assertThrows("entered pattern \"" + textToSend + "\" is invalid", RuntimeException.class, () -> {
            XcMailrApi.fetchEmails(emailUnderTest, null, null, textToSend, null, true);
        });
    }

    /**
     * assert that the validity of the pattern for html content will be checked before to make a request on server
     */
    @Test
    public void fetchLastEmailByHtmlContentWithInvalidPattern()
    {
        Assert.assertThrows("entered pattern \"" + textToSend + "\" is invalid", RuntimeException.class, () -> {
            XcMailrApi.fetchEmails(emailUnderTest, null, null, null, textToSend, true);
        });
    }

    /**
     * test that it's possible to fetch all received e-mails
     */
    @Test
    public void fetchEmailsFromTempEmail()
    {
        SendEmail.send(emailAccount, emailUnderTest, subject, textToSend);
        Selenide.sleep(1000);

        // fetch all received e-mails
        List<Mail> mails = XcMailrApi.fetchEmails(emailUnderTest, null, null, null, null, false);
        Assert.assertEquals(2, mails.size());

        for (Mail mail : mails)
        {
            validateMessage(mail);
        }
    }

    private void validateMessage(Mail message)
    {
        Assert.assertEquals(message.recipient, emailUnderTest);
        Assert.assertEquals(message.sender, emailAccount.getEmail());
        Assert.assertEquals(message.subject, subject);
        Assert.assertEquals(decodeAndNormalize(message.textContent), textToSend);
        Assert.assertEquals(decodeAndNormalize(message.htmlContent), textToSend);
    }
}
