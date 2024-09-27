package com.xceptance.neodymium.plugin.xcmailr;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.neodymium.plugin.xcmailr.data.EmailAccount;
import com.xceptance.neodymium.plugin.xcmailr.util.SendEmail;

import xcmailr.client.Attachment;
import xcmailr.client.Mail;

public class XcMailrRetrieveEmailsTest extends AbstractXcMailrApiTest
{
    private final String subject = "Test";

    private final String textToSend = "Hi\nHow are you?)\nBye";

    private final File fileToSend = new File("src/test/resources/com/xceptance/neodymium/plugin/xcmailr/message.txt");

    private final EmailAccount emailAccount = new EmailAccount(CREDENTIALS.smtpServerEmail(), CREDENTIALS.smtpServerLogin(),
                                                               CREDENTIALS.smtpServerPassword(), CREDENTIALS.smtpServerHost(),
                                                               CREDENTIALS.smtpServerPort(), false, true);

    @Before
    public void createTempEmailAndSendMessage() throws IOException, URISyntaxException, InterruptedException
    {
        XcMailrApi.createTemporaryEmail(emailUnderTest, false);

        SendEmail.send(emailAccount, emailUnderTest, subject, textToSend, fileToSend);
    }

    /**
     * test that the method <code>XcMailrApi.retrieveLastEmailBySubject</code> works correct</br>
     * in other words, that it's possible to fetch last received e-mail with a specific subject
     */
    @Test
    public void testRetrieveLastEmailBySubject()
    {
        Mail mail = XcMailrApi.retrieveLastEmailBySubject(emailUnderTest, subject);
        validateMessage(mail);
    }

    @Test
    public void testRetrieveLastEmailBySubjectExpectNull()
    {
        Mail mail = XcMailrApi.retrieveLastEmailBySubject(emailUnderTest, "YouCantFindMe");
        Assert.assertNull(mail);
    }

    /**
     * test that the method <code>XcMailrApi.retrieveLastEmailBySender</code> works correct</br>
     * in other words, that it's possible to fetch last received e-mail with a specific sender
     * 
     * @throws IOException
     */
    @Test
    public void testRetrieveAttachmentFromLastEmail() throws IOException
    {
        Mail mail = XcMailrApi.retrieveLastEmailBySender(emailUnderTest, emailAccount.getEmail());
        validateMessage(mail);

        Attachment attachment = mail.attachments.get(0);
        String attachmentName = attachment.name;
        File fileRecieved = new File("./target/attachmentTest-" + attachmentName);
        fileRecieved.deleteOnExit();
        XcMailrApi.fetchAttachment(mail, attachmentName, fileRecieved);
        Assert.assertTrue(fileRecieved.exists());
        Assert.assertTrue(FileUtils.contentEquals(fileToSend, fileRecieved));
    }

    /**
     * test that the method <code>XcMailrApi.retrieveLastEmailBySender</code> works correct</br>
     * in other words, that it's possible to fetch last received e-mail with a specific sender
     */
    @Test
    public void testRetrieveLastEmailBySender()
    {
        Mail mail = XcMailrApi.retrieveLastEmailBySender(emailUnderTest, emailAccount.getEmail());
        validateMessage(mail);
    }

    @Test
    public void testRetrieveLastEmailBySendertExpectNull()
    {
        Mail mail = XcMailrApi.retrieveLastEmailBySubject(emailUnderTest, "YouCantFindMe");
        Assert.assertNull(mail);
    }

    /**
     * test that it's possible to fetch the last received e-mail
     */
    @Test
    public void fetchLastEmail()
    {
        // fetch last received e-mail
        Mail mail = XcMailrApi.fetchEmails(emailUnderTest, null, null, null, null, null, true, null).get(0);
        validateMessage(mail);
    }

    /**
     * test that it's possible to fetch last received e-mail with a specific plain text
     */
    @Test
    public void fetchLastEmailBySender()
    {
        // fetch last received e-mail with specified plain text
        Mail mail = XcMailrApi.fetchEmails(emailUnderTest, emailAccount.getEmail(), null, null, null, null, true, null).get(0);
        validateMessage(mail);
    }

    /**
     * test that it's possible to fetch last received e-mail after specific date
     */
    @Test
    public void fetchLastEmailByLastDate()
    {
        List<Mail> mail = XcMailrApi.fetchEmails(emailUnderTest, null, null, null, null, null, true, DateUtils.addMinutes(new Date(), 1));
        Assert.assertNull(mail);
        Date now = new Date();
        SendEmail.send(emailAccount, emailUnderTest, subject, textToSend, fileToSend);
        mail = XcMailrApi.fetchEmails(emailUnderTest, null, null, null, null, null, true, now);
        Assert.assertTrue(mail.size() == 1);
        validateMessage(mail.get(0));
    }

    /**
     * test the {@link XcMailrApi#getTimestampOfLastEmail(String)} methods return correct time
     */
    @Test
    public void testGetTimestampOfLastEmail()
    {
        long date = XcMailrApi.fetchEmails(emailUnderTest, emailAccount.getEmail(), null, null, null, null, true, null).get(0).receivedTime;

        Date lastEmailDate = XcMailrApi.getTimestampOfLastEmail(emailUnderTest);
        Assert.assertTrue(date - lastEmailDate.getTime() < 1000);
    }

    /**
     * test that it's possible to fetch last received e-mail with a specific plain text
     */
    @Test
    public void fetchLastEmailBySubject()
    {
        // fetch last received e-mail with specified plain text
        Mail mail = XcMailrApi.fetchEmails(emailUnderTest, null, subject, null, null, null, true, null).get(0);
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
        Mail mail = XcMailrApi.fetchEmails(emailUnderTest, null, null, textToRetrieve, null, null, true, null).get(0);
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
        Mail mail = XcMailrApi.fetchEmails(emailUnderTest, null, null, null, textToRetrieve, null, true, null).get(0);
        validateMessage(mail);
    }

    /**
     * assert that the validity of the pattern for text content will be checked before to make a request on server
     */
    @Test
    public void fetchLastEmailByTextContentWithInvalidPattern()
    {
        Assert.assertThrows("entered pattern \"" + textToSend + "\" is invalid", RuntimeException.class, () -> {
            XcMailrApi.fetchEmails(emailUnderTest, null, null, textToSend, null, null, true, null);
        });
    }

    /**
     * assert that the validity of the pattern for html content will be checked before to make a request on server
     */
    @Test
    public void fetchLastEmailByHtmlContentWithInvalidPattern()
    {
        Assert.assertThrows("entered pattern \"" + textToSend + "\" is invalid", RuntimeException.class, () -> {
            XcMailrApi.fetchEmails(emailUnderTest, null, null, null, textToSend, null, true, null);
        });
    }

    /**
     * test that it's possible to fetch all received e-mails
     */
    @Test
    public void fetchEmailsFromTempEmail()
    {
        SendEmail.send(emailAccount, emailUnderTest, subject, textToSend, fileToSend);
        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        // fetch all received e-mails
        List<Mail> mails = XcMailrApi.fetchEmails(emailUnderTest, null, null, null, null, null, false, null);
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
        Assert.assertFalse(message.attachments.isEmpty());
    }
}
