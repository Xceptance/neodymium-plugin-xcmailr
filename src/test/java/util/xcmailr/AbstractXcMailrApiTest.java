package util.xcmailr;

import java.io.IOException;

import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.util.Base64;
import org.apache.http.client.ClientProtocolException;
import org.junit.After;
import org.junit.Before;

import util.xcmailr.util.Credentials;
import util.xcmailr.util.SendRequest;

public class AbstractXcMailrApiTest extends AbstractTest
{
    protected final String xcmailrEmail = CREDENTIALS.xcmailrEmail();

    protected final String xcmailrPassword = CREDENTIALS.xcmailrPassword();

    protected static final Credentials CREDENTIALS = ConfigFactory.create(Credentials.class, System.getenv());

    protected final String tempEmail = "testTest1@varmail.net";

    @Before
    public void configureApiToken()
    {
        final String apiToken = System.getenv("XCMAILR_TOKEN");
        if (apiToken != null)
        {
            writeProperty("xcmailr.apiToken", apiToken);
        }
        writeProperty("xcmailr.temporaryMailValidMinutes", "3");
        SendRequest.login(xcmailrEmail, xcmailrPassword);
    }

    @After
    public void deleteTempEmail() throws ClientProtocolException, IOException
    {
        SendRequest.deleteTempEmail(tempEmail);
    }

    protected String decode(String text)
    {
        return new String(Base64.decode(text)).replaceAll(String.valueOf((char) 13), "");
    }
}
