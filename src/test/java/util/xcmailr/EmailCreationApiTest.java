package util.xcmailr;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import util.xcmailr.util.SendRequest;

public class EmailCreationApiTest extends AbstractXcmailrApiTest
{
    @Test
    public void testEmailCreated() throws IOException
    {
        XcMailrApi.createTemporaryEmail(tempEmail);
        Assert.assertTrue(SendRequest.emailExists(tempEmail));
    }

    @Test
    public void testEmailExpired() throws IOException
    {
        String response = XcMailrApi.createTemporaryEmail(tempEmail);

        Pattern validMinutesPatters = Pattern.compile("<div class=\"email-validity-minutes\">(\\d)</div>");
        Matcher matcher = validMinutesPatters.matcher(response);
        while (matcher.find())
        {
            Assert.assertEquals(validMinutes, matcher.group(1));
        }
    }
}
