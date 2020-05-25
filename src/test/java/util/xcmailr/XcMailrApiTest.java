package util.xcmailr;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.codeborne.selenide.Selenide;
import com.xceptance.neodymium.NeodymiumRunner;

import util.xcmailr.util.SendRequest;

@RunWith(NeodymiumRunner.class)
public class XcMailrApiTest extends AbstractXcMailrApiTest
{

    @Test
    public void testEmailCreated()
    {
        XcMailrApi.createTemporaryEmail(tempEmail);
        Assert.assertTrue(SendRequest.emailExists(tempEmail));
    }

    @Test
    public void testEmailExpired()
    {
        writeProperty("xcmailr.temporaryMailValidMinutes", "1");
        XcMailrApi.createTemporaryEmail(tempEmail);
        Assert.assertFalse(SendRequest.emailExpired(tempEmail));
        Selenide.sleep(120000);
        assertTrue(SendRequest.emailExpired(tempEmail));
    }
}