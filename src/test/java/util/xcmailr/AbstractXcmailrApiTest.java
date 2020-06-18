package util.xcmailr;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

import org.aeonbits.owner.ConfigFactory;
import org.apache.http.client.ClientProtocolException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import util.xcmailr.util.Credentials;
import util.xcmailr.util.SendRequest;

public abstract class AbstractXcmailrApiTest extends AbstractTest
{
    protected static final Credentials CREDENTIALS = ConfigFactory.create(Credentials.class, System.getenv());

    protected static final String validMinutes = "1";

    protected String tempEmail;

    @BeforeClass
    public static void configureApiToken() throws ClientProtocolException, IOException
    {
        final String apiToken = System.getenv("XCMAILR_TOKEN");
        if (apiToken != null)
        {
            properties.put("xcmailr.apiToken", apiToken);
        }
        properties.put("xcmailr.temporaryMailValidMinutes", validMinutes);
        savePropertiesAndApply();

        SendRequest.login(CREDENTIALS.xcmailrEmail(), CREDENTIALS.xcmailrPassword());
    }

    @Before
    public void createTempEmail()
    {
        tempEmail = randomEmail("test", "varmail.net");
    }

    @After
    public void deleteTempEmail() throws ClientProtocolException, IOException
    {
        SendRequest.deleteTempEmail(tempEmail);
    }

    protected static String decodeAndNormalize(String text)
    {
        return new String(Base64.getDecoder().decode(text)).replaceAll(String.valueOf((char) 13), "");
    }

    protected static String randomEmail(String prefix, String domain)
    {
        final String uuid = UUID.randomUUID().toString();
        final String data = uuid.replaceAll("-", "");
        final StringBuilder sb = new StringBuilder(42);

        sb.append(prefix);
        sb.append(data.concat(data).substring(0, 12));
        sb.append("@");
        sb.append(domain);

        return sb.toString().toLowerCase();
    }
}
