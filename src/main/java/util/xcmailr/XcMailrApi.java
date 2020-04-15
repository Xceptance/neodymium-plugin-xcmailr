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
     * Retrieves the instance of applitools configuration for the current thread.
     * 
     * @return the configuration instance for the current thread
     */
    public static XcMailrConfiguration getConfiguration()
    {
        // the property needs to be a valid URI in order to satisfy the Owner framework
        if (null == ConfigFactory.getProperty(TEMPORARY_CONFIG_FILE_PROPERTY_NAME))
        {
            ConfigFactory.setProperty(TEMPORARY_CONFIG_FILE_PROPERTY_NAME, "file:this/path/should/never/exist/noOneShouldCreateMe.properties");
        }
        return CONFIGURATION.computeIfAbsent(Thread.currentThread(), key -> {
            return ConfigFactory.create(XcMailrConfiguration.class);
        });
    }

    public static void createTemporaryEmail(String email)
    {
        String url = getConfiguration().url() + "/create/temporaryMail/" + getConfiguration().apiToken() + "/" + email + "/"
                     + getConfiguration().temporaryMailValidMinutes();
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();
        Response response = callXcMailr(builder.build());

        Assert.assertNotNull("XcMailr not reachable", response);
        Assert.assertEquals("Temporary Email could not be created", 200, response.code());
        LOGGER.debug("Email created: \"" + email + "\"");
        response.close();
    }

    public static String retrieveLastEmailBySubject(String email, String subject)
    {
        return fetchEmails(email, null, subject, null, null, null, true);
    }

    public static String retrieveLastEmailBySender(String email, String sender)
    {
        return fetchEmails(email, sender, null, null, null, null, true);
    }

    public static String fetchEmails(String email, String from, String subject, String textContent, String htmlContent, String format,
                                     boolean lastMatch)
    {
        final int maxFailures = getConfiguration().maximumWaitingTime() * 60 / getConfiguration().pollingInterval();
        int failCount = 0;
        String lastResult = null;
        while (true)
        {
            // quit if failed for more than 3 times
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
            catch (Exception e)
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
            catch (InterruptedException e)
            {
                // quit if interrupted
                LOGGER.error("Interrupted");
            }
        }
    }

    private static Response fetchEmailsFromRemote(String email, String from, String subject, String textContent, String htmlContent, String format,
                                                  boolean lastMatch)
    {
        String url = getConfiguration().url() + "/mailbox/" + "/" + email + "/" + getConfiguration().apiToken();
        HttpUrl.Builder builder = HttpUrl.parse(url).newBuilder();

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

        Response response = callXcMailr(builder.build());

        Assert.assertNotNull("XcMailr not reachable", response);
        Assert.assertEquals("Temporary Email could not be created", 200, response.code());

        return response;
    }

    private static Response callXcMailr(HttpUrl httpUrl)
    {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(httpUrl).build();
        try
        {
            return client.newCall(request).execute();
        }
        catch (IOException e)
        {
            LOGGER.error("Request error while callincg XcMailr.");
            e.printStackTrace();
        }
        return null;
    }
}
