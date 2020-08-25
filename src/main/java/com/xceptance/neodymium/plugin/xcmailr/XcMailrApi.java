package com.xceptance.neodymium.plugin.xcmailr;

import java.net.http.HttpClient;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xcmailr.client.Mail;
import xcmailr.client.MailFilterOptions;
import xcmailr.client.Mailbox;
import xcmailr.client.XCMailrApiException;
import xcmailr.client.XCMailrClient;

public class XcMailrApi
{
    private static Logger LOGGER = LoggerFactory.getLogger(XcMailrApi.class);

    private static final Map<Thread, XcMailrConfiguration> CONFIGURATION = Collections.synchronizedMap(new WeakHashMap<>());

    public final static String TEMPORARY_CONFIG_FILE_PROPERTY_NAME = "xcmailr.temporaryConfigFile";

    /**
     * Retrieves the {@link XcMailrConfiguration} instance of the current thread.
     * 
     * @return the XcMailrConfiguration instance of the current thread
     */
    public static XcMailrConfiguration getConfiguration()
    {
        // the property needs to be a valid URI in order to satisfy the Owner framework
        if (null == ConfigFactory.getProperty(TEMPORARY_CONFIG_FILE_PROPERTY_NAME))
        {
            ConfigFactory.setProperty(TEMPORARY_CONFIG_FILE_PROPERTY_NAME,
                                      "file:this/path/should/never/exist/noOneShouldCreateMe.properties");
        }
        return CONFIGURATION.computeIfAbsent(Thread.currentThread(), key -> {
            return ConfigFactory.create(XcMailrConfiguration.class);
        });
    }

    /**
     * Retrieves the {@link XCMailrClient} instance of the current thread.
     * 
     * @return the XCMailrClient instance of the current thread
     */
    private static XCMailrClient getXCMailrClient()
    {
        return new XCMailrClient(getConfiguration().url(), getConfiguration().apiToken(), HttpClient.newHttpClient());
    }

    /**
     * Creates a temporary e-mail. <br>
     * The validity period is determined from the configuration.
     * 
     * @param email
     *            the e-mail address <br>
     *            The domain of the e-mail must match the XcMailr's available domains.
     * @param forwardEnabled
     *            boolean value if the received e-mails should be forwarded to an account owner e-mail
     * @return a {@link Mailbox} object containing information about the temporary e-mail
     */
    public static Mailbox createTemporaryEmail(String email, boolean forwardEnabled)
    {
        Mailbox mailbox = new Mailbox(email, DateUtils.addMinutes(new Date(), getConfiguration().temporaryMailValidMinutes()).getTime(), forwardEnabled);
        try
        {
            mailbox = getXCMailrClient().mailboxes().createMailbox(mailbox);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error while creating the mailbox.", e);
        }
        return mailbox;
    }

    /**
     * Retrieves information about all users mailboxes
     * 
     * @return the retrieved {@link List} of {@link Mailbox} objects
     */
    public static List<Mailbox> listMailboxes()
    {
        List<Mailbox> mailboxes = null;
        try
        {
            mailboxes = getXCMailrClient().mailboxes().listMailboxes();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error while retrieving and parsing the mails.", e);
        }
        return mailboxes;
    }

    /**
     * Retrieves information about mailbox
     * 
     * @param email
     *            e-mail address of the target mailbox
     * @return @link Mailbox} object with the desired information
     */
    public static Mailbox getMailbox(String email)
    {
        Mailbox mailbox = null;

        try
        {
            mailbox = getXCMailrClient().mailboxes().getMailbox(email);
        }
        catch (Exception e)
        {
            if (e instanceof XCMailrApiException && ((XCMailrApiException) e).statusCode == 404)
            {
                return mailbox;
            }
            else
            {
                throw new RuntimeException("Error while retrieving the mailbox.", e);
            }
        }
        return mailbox;
    }

    /**
     * Update information for the mailbox
     * 
     * @param address
     *            current address of the mailbox
     * @param newAddress
     *            new mailbox address
     * @param minutesActive
     *            period during which the temporary e-mail should be active
     * @param forwardEnabled
     *            boolean value if the received e-mails should be forwarded to an account owner e-mail
     * @return @link Mailbox} object with information about updated mailbox
     */
    public static Mailbox updateMailbox(final String address, final String newAddress, final Integer minutesActive,
                                        final Boolean forwardEnabled)
    {
        Mailbox oldMailbox = getMailbox(address);
        int validMinutes = (int) Math.round((new Date(oldMailbox.deactivationTime).getTime() - new Date().getTime() * 1.0) / 60000);
        Mailbox mailbox = null;

        try
        {
            mailbox = getXCMailrClient().mailboxes().updateMailbox(address, newAddress == null ? oldMailbox.address : newAddress,
                                                                   minutesActive == null ? validMinutes : minutesActive,
                                                                   forwardEnabled == null ? oldMailbox.forwardEnabled : forwardEnabled);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error while updating the mailbox.", e);
        }
        return mailbox;
    }

    /**
     * Update the mailbox address
     * 
     * @param address
     *            current address of the mailbox
     * @param newAddress
     *            new mailbox address
     * @return @link Mailbox} object with information about updated mailbox
     */
    public static Mailbox updateMailboxEmailAddress(final String address, final String newAddress)
    {
        return updateMailbox(address, newAddress, null, null);
    }

    /**
     * Update information for the mailbox
     * 
     * @param address
     *            current address of the mailbox
     * @param newAddress
     *            new mailbox address
     * @return @link Mailbox} object with information about updated mailbox
     */
    public static Mailbox updateMailboxDeactivationTime(final String address, final int newValidMinutesNumber)
    {
        return updateMailbox(address, null, newValidMinutesNumber, null);
    }

    /**
     * Update forward settings for the mailbox
     * 
     * @param address
     *            current address of the mailbox
     * @param newAddress
     *            new mailbox address
     * @return @link Mailbox} object with information about updated mailbox
     */
    public static Mailbox updateMailboxForwarding(final String address, final boolean forwardEnabled)
    {
        return updateMailbox(address, null, null, forwardEnabled);
    }

    /**
     * Delete the mailbox
     * 
     * @param address
     *            e-mail of the mailbox, that should be deleted
     */
    public static void deleteMailbox(String address)
    {
        try
        {
            if (getMailbox(address) != null)
            {
                getXCMailrClient().mailboxes().deleteMailbox(address);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error while deleting the mailbox", e);
        }
    }

    // ----------------------------- Mail Api-------------------------------------------
    /**
     * Retrieve all mail from mailbox
     * 
     * @param mailboxAddress
     *            mailbox address
     * @return the retrieved {@link List} of {@link Mail} objects
     */
    public static List<Mail> retrieveAllMailsFromMailbox(String mailboxAddress)
    {
        return fetchEmails(mailboxAddress, null, null, null, null, null, false);
    }

    /**
     * Gets the last received e-mail with the specified subject.
     * 
     * @param email
     *            the e-mail address which should receive the expected e-mail
     * @param subject
     *            the received e-mail's subject. May also be a regular expression.
     * @return a String containing a JSON array with the received message
     */
    public static Mail retrieveLastEmailBySubject(String email, String subject)
    {
        return fetchEmails(email, null, subject, null, null, null, true).get(0);
    }

    /**
     * Gets the last received e-mail from the specified sender.
     * 
     * @param email
     *            the e-mail address which should receive the expected e-mail
     * @param sender
     *            the received e-mail's sender. May also be a regular expression.
     * @return a String containing a JSON array of the received message
     */
    public static Mail retrieveLastEmailBySender(String email, String sender)
    {
        return fetchEmails(email, sender, null, null, null, null, true).get(0);
    }

    /**
     * Gets all received e-mails which match the specified parameters
     * 
     * @param email
     *            the e-mail address which should receive the expected e-mails.
     * @param from
     *            the received e-mail's sender. May also be a regular expression.
     * @param subject
     *            the received e-mail's subject. May also be a regular expression.
     * @param textContent
     *            the text content of the e-mail. May also be a regular expression.
     * @param htmlContent
     *            the HTML content of the e-mail. May also be a regular expression.
     * @param headers
     *            the received e-mail's header. May also be a regular expression.
     * @param lastMatch
     *            a boolean indicating whether only the last e-mail or more should be returned.
     * @return a String containing JSON Objects of each e-mail
     */
    public static List<Mail> fetchEmails(String email, String from, String subject, String textContent, String htmlContent, String headers,
                                         boolean lastMatch)
    {
        assertPatternIsValid(from);
        assertPatternIsValid(subject);
        assertPatternIsValid(textContent);
        assertPatternIsValid(htmlContent);
        assertPatternIsValid(headers);
        final int maxFailures = getConfiguration().maximumWaitingTime() * 60 / getConfiguration().pollingInterval();
        int failCount = 0;
        while (true)
        {
            // quit if failed for more than maxFailures times
            if (failCount >= maxFailures)
            {
                LOGGER.warn("No e-mail retrieved while polling.");
                return null;
            }
            List<Mail> mails = fetchEmailsFromRemote(email, from, subject, textContent, htmlContent, headers, lastMatch);
            if (mails != null && !mails.isEmpty())
            {
                return mails;
            }
            failCount++;

            try
            {
                Thread.sleep(getConfiguration().pollingInterval() * 1000);
            }
            catch (final InterruptedException e)
            {
                // quit if interrupted
                LOGGER.error("Interrupted");
            }
        }
    }

    private static List<Mail> fetchEmailsFromRemote(String email, String from, String subject, String textContent, String htmlContent, String headers,
                                                    boolean lastMatch)
    {
        MailFilterOptions filters = new MailFilterOptions();

        if (StringUtils.isNotBlank(from))
        {
            filters.senderPattern(from);
        }
        if (StringUtils.isNotBlank(subject))
        {
            filters.subjectPattern(subject);
        }
        if (StringUtils.isNotBlank(textContent))
        {
            filters.textContentPattern(textContent);
        }
        if (StringUtils.isNotBlank(htmlContent))
        {
            filters.htmlContentPattern(htmlContent);
        }
        if (StringUtils.isNotBlank(htmlContent))
        {
            filters.headersPattern(headers);
        }
        filters.lastMatchOnly(lastMatch);
        List<Mail> mails = null;
        try
        {
            mails = getXCMailrClient().mails().listMails(email, filters);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error while retrieving and parsing the mails.", e);
        }

        return mails;
    }

    private static void assertPatternIsValid(String pattern)
    {
        if (pattern != null)
        {
            try
            {
                Pattern.compile(pattern);
            }
            catch (PatternSyntaxException e)
            {
                throw new RuntimeException("The given pattern \"" + pattern + "\" is invalid");
            }
        }
    }

    private static MailboxApiImpl createMailboxApiImpl()
    {
        final HttpClient httpClient = HttpClient.newHttpClient();
        RestApiClient restApiClient = new RestApiClient(getConfiguration().url(), getConfiguration().apiToken(), httpClient);

        return new MailboxApiImpl(restApiClient, new Gson());
    }

    private static MailApiImpl createMailApiImpl()
    {
        HttpClient httpClient = HttpClient.newHttpClient();
        RestApiClient restApiClient = new RestApiClient(getConfiguration().url(), getConfiguration().apiToken(), httpClient);

        return new MailApiImpl(restApiClient, new Gson());
    }
}
