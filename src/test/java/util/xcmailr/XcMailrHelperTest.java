package util.xcmailr;

import static com.codeborne.selenide.Selenide.$;

import org.aeonbits.owner.util.Base64;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.xceptance.neodymium.NeodymiumRunner;
import com.xceptance.neodymium.module.statement.browser.multibrowser.Browser;

@RunWith(NeodymiumRunner.class)
public class XcMailrHelperTest extends AbstractTest
{
    private final String text = "Hi\n\nHow are you?)\n\nBye";

    private final String textHtml = "<div dir=\"auto\">Hi<div dir=\"auto\"><br></div><div dir=\"auto\">How are you?)</div><div dir=\"auto\"><br></div><div dir=\"auto\">Bye</div></div>";

    private final String response = "[{'mailAddress':'foo.bar@de.com'," + "'sender':'foo@bar.com'," + "'subject':'test'," +
                                    "'receivedTime':1586959433701," + "'attachments':[]," +
                                    "'downloadToken':'8f737e8c-8df3-41ab-a3f2-50da4542244b'," + "'htmlContent':'" + Base64.encode(textHtml.getBytes()) + "'," +
                                    "'textContent':'" + Base64.encode(text.getBytes()) + "'}]";

    @Test
    public void testGetFirstMailsTextContent()
    {
        Assert.assertEquals(text, XcMailrHelper.getFirstMailsTextContent(response));
    }

    @Test
    public void testGetFirstMailsHtmlContent()
    {
        Assert.assertEquals(textHtml, XcMailrHelper.getFirstMailsHtmlContent(response));
    }

    @Browser("Chrome_headless")
    @Test
    public void testOpenHtmlContentWithCurrentWebDriver()
    {
        XcMailrHelper.openHtmlContentWithCurrentWebDriver(textHtml);
        Assert.assertEquals(text, $("body").getText());
    }
}
