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
     * Reset configuration properties for thread. Needed for unit tests to enable reset of configurations for individual
     * test methods, which belong to one test class
     */
    protected static void resetConfigurationsForThread()
    {
        CONFIGURATION.put(Thread.currentThread(), ConfigFactory.create(XcMailrConfiguration.class));
    }

    /**
     * Create temporary email with validity time stated in xcmailr.properties under xcmailr.temporaryMailValidMinutes
     * option
     * 
     * @param email
     *            full temporary mail's name with domain </br>
     *            mind, that not all domains are acceptable acceptable domains : mailsink.xceptance.de, varmail.net,
     *            varmail.co.uk, varmailservice.com, var-mail.com, mail.varmail.net, varmail.de, varmail.online,
     *            varmail.international
     */
    public static void createTemporaryEmail(String email)
    {
        final String url = getConfiguration().url() + "/create/temporaryMail/" + getConfiguration().apiToken() + "/" + email + "/" +
                           getConfiguration().temporaryMailValidMinutes();
        final HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        final Response response = callXcMailr(builder.build());

        Assert.assertNotNull("XcMailr not reachable", response);
        Assert.assertEquals("Temporary Email could not be created", 200, response.code());
        LOGGER.debug("Email created: \"" + email + "\"");
        response.close();
    }

    /**
     * Get last received email with specified subject
     * 
     * @param email
     *            email address on which expected email should arrive
     * @param subject
     *            regular expression to find in the emails subject
     * @return json object of received message
     */
    public static String retrieveLastEmailBySubject(String email, String subject)
    {
        return fetchEmails(email, null, subject, null, null, null, true);
    }

    /**
     * Get last received email from specified sender
     * 
     * @param email
     *            email address on which expected email should arrive
     * @param sender
     *            a regular expression to find in the address the mail was sent from
     * @return json object of received message
     */
    public static String retrieveLastEmailBySender(String email, String sender)
    {
        return fetchEmails(email, sender, null, null, null, null, true);
    }

    /**
     * Get received email, which matches specified parameters
     * 
     * @param email
     *            email address on which expected email should arrive
     * @param from
     *            a regular expression to find in the address the mail was sent from
     * @param subject
     *            regular expression to find in the emails subject
     * @param textContent
     *            a regular expression to find in the emails text content
     * @param htmlContent
     *            a regular expression to find in the emails html content
     * @param format
     *            a string indicating the desired response format. Valid values are "html", "json" and "header".
     * @param lastMatch
     *            a parameter without value that limits the result set to one entry. This is the last filter that will
     *            be applied to result set.
     * @return object of the received message in format specified in parameters
     */
    public static String fetchEmails(String email, String from, String subject, String textContent, String htmlContent, String format,
                                     boolean lastMatch)
    {
        final int maxFailures = getConfiguration().maximumWaitingTime() * 60 / getConfiguration().pollingInterval();
        int failCount = 0;
        String lastResult = null;
        while (true)
        {
            // quit if failed for more than maxFailures times
            if (failCount >= maxFailures)
            {
                LOGGER.warn("No email retrieved while polling.");
                return lastResult;
            }

            Response response = null;
            try
            {
                response = fetchEmailsFromRemote(email, from, subject, textContent, htmlContent, format, lastMatch);
                if (response.isSuccessful())
                {
                    lastResult = response.body().string();
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

        Assert.assertEquals("Temporary Email could not be accessed", 200, response.code());

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
