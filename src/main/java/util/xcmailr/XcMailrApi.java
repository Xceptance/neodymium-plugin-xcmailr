package util.xcmailr;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
     * Creates a temporary e-mail. <br>
     * The validity period is determined from the configuration.
     * 
     * @param email
     *            the e-mail address <br>
     *            The domain of the e-mail must match the XcMailr's available domains.
     * @return a String containing the server's response with information about the temporary e-mail
     * @throws IOException
     *             in case if there was an error while reading the response from server
     */
    public static String createTemporaryEmail(String email)
    {
        final String url = getConfiguration().url() + "/create/temporaryMail/" + getConfiguration().apiToken() + "/" + email + "/" +
                           getConfiguration().temporaryMailValidMinutes();
        final HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        final Response response = callXcMailr(builder.build());
        String responseBody = "";
        try
        {
            responseBody = response.body().string();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        Assert.assertNotNull("XcMailr not reachable", response);
        Assert.assertEquals("Temporary Email could not be created", 200, response.code());
        LOGGER.debug("E-mail created: \"" + email + "\"");
        response.close();
        return responseBody;
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
    public static String retrieveLastEmailBySubject(String email, String subject)
    {
        return fetchEmails(email, null, subject, null, null, null, true);
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
    public static String retrieveLastEmailBySender(String email, String sender)
    {
        return fetchEmails(email, sender, null, null, null, null, true);
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
     * @param format
     *            a String indicating the desired response format. Valid values are "html", "json" and "header".
     * @param lastMatch
     *            a boolean indicating whether only the last e-mail or more should be returned.
     * @return a String containing JSON Objects of each e-mail
     */
    public static String fetchEmails(String email, String from, String subject, String textContent, String htmlContent, String format,
                                     boolean lastMatch)
    {
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

            Response response = null;
            try
            {
                response = fetchEmailsFromRemote(email, from, subject, textContent, htmlContent, format, lastMatch);
                if (response.isSuccessful())
                {
                    String lastResult = response.body().string();
                    if (StringUtils.isNotBlank(lastResult) && !lastResult.equals("[]"))
                    {
                        // success
                        return lastResult;
                    }
                }
            }
            catch (final Exception e)
            {
                LOGGER.error("Error while analizing the request.");
            }
            finally
            {
                if (response != null)
                {
                    response.close();
                }
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

    private static Response fetchEmailsFromRemote(String email, String from, String subject, String textContent, String htmlContent,
                                                  String format, boolean lastMatch)
    {
        final String url = getConfiguration().url() + "/mailbox/" + "/" + email + "/" + getConfiguration().apiToken();
        final HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();

        if (StringUtils.isNotBlank(from))
        {
            builder.addQueryParameter("from", from);
        }
        if (StringUtils.isNotBlank(subject))
        {
            builder.addQueryParameter("subject", subject);
        }
        if (StringUtils.isNotBlank(textContent))
        {
            builder.addQueryParameter("textContent", textContent);
        }
        if (StringUtils.isNotBlank(htmlContent))
        {
            builder.addQueryParameter("htmlContent", htmlContent);
        }
        if (StringUtils.isNotBlank(format))
        {
            builder.addQueryParameter("format", format);
        }
        else
        {
            builder.addQueryParameter("format", "json");
        }
        if (lastMatch)
        {
            builder.addQueryParameter("lastMatch", "");
        }
        final Response response = callXcMailr(builder.build());

        Assert.assertNotNull("XcMailr not reachable", response);

        Assert.assertEquals("Mailbox could not be accessed", 200, response.code());

        return response;
    }

    private static Response callXcMailr(HttpUrl httpUrl)
    {
        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder().url(httpUrl).build();
        try
        {
            return client.newCall(request).execute();
        }
        catch (final IOException e)
        {
            LOGGER.error("Request error while callincg XcMailr.");
            e.printStackTrace();
        }
        return null;
    }
}
