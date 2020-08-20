package util.xcmailr;

import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aeonbits.owner.ConfigFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import util.xcmailr.util.Credentials;

public abstract class AbstractXcMailrApiTest extends AbstractTest
{
    protected static final Credentials CREDENTIALS = ConfigFactory.create(Credentials.class, System.getenv());

    protected static final String VALID_MINUTES = "3";

    protected static final String EMAIL_PREFIX = "xcmailr-plugin-test-";

    protected static final String EMAIL_SUFFIX = "varmail.de";

    protected String emailUnderTest;

    @BeforeClass
    public static void configureApiToken()
    {
        final String apiToken = System.getenv("XCMAILR_TOKEN");
        if (apiToken != null)
        {
            properties.put("xcmailr.apiToken", apiToken);
        }
        properties.put("xcmailr.temporaryMailValidMinutes", VALID_MINUTES);

        savePropertiesAndApply();
    }

    @Before
    public void createTempEmail()
    {
        emailUnderTest = randomEmail(EMAIL_PREFIX, EMAIL_SUFFIX);
    }

    @After
    public void deleteTempEmail()
    {
        XcMailrApi.deleteMailbox(emailUnderTest);
    }

    protected static String decodeAndNormalize(String text)
    {
        Pattern isBase64 = Pattern.compile("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$");
        Matcher matcher = isBase64.matcher(text);
        String decodedText = text;
        if (matcher.find())
        {
            decodedText = new String(Base64.getDecoder().decode(text));
        }
        return decodedText.replaceAll(String.valueOf((char) 13), "");
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
