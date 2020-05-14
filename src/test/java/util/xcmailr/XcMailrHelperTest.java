package util.xcmailr;

import static com.codeborne.selenide.Selenide.$;

import org.aeonbits.owner.util.Base64;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.xceptance.neodymium.NeodymiumRunner;
import com.xceptance.neodymium.module.statement.browser.multibrowser.Browser;

import util.xcmailr.util.Base64Decoder;

@RunWith(NeodymiumRunner.class)
public class XcMailrHelperTest extends AbstractTest
{
    // TODO use unencoded string and encode it when needed
    private final String textBase64 = "SGkNCg0KSG93IGFyZSB5b3U/KQ0KDQpCeWUNCg==";

    // TODO use unencoded string and encode it when needed
    private final String textHtmlBase64 = "PGRpdiBkaXI9ImF1dG8iPkhpPGRpdiBkaXI9ImF1dG8iPjxicj48L2Rpdj48ZGl2IGRpcj0iYXV0byI+SG93IGFyZSB5b3U/KTwvZGl2PjxkaXYgZGlyPSJhdXRvIj48YnI+PC9kaXY+PGRpdiBkaXI9ImF1dG8iPkJ5ZTwvZGl2PjwvZGl2Pg0K";

    private final String response = "[{'mailAddress':'foo.bar@de.com'," + "'sender':'foo@bar.com'," + "'subject':'test'," +
                                    "'receivedTime':1586959433701," + "'attachments':[]," +
                                    "'downloadToken':'8f737e8c-8df3-41ab-a3f2-50da4542244b'," + "'htmlContent':'" + textHtmlBase64 + "'," +
                                    "'textContent':'" + textBase64 + "'}]";

    @Test
    public void testGetFirstMailsTextContent()
    {
        final String text = new String(Base64.decode(textBase64));

        Assert.assertEquals(text, XcMailrHelper.getFirstMailsTextContent(response));
    }

    @Test
    // FIXME typo
    public void testGetFirstMailsHmlContent()
    {
        final String textHtml = new String(Base64.decode(textHtmlBase64));

        Assert.assertEquals(textHtml, XcMailrHelper.getFirstMailsHtmlContent(response));
    }

    @Browser("Chrome_headless")
    @Test
    public void testOpenHtmlContentWithCurrentWebDriver()
    {
        final String textHtml = Base64Decoder.decode(textHtmlBase64);

        final String expectedText = Base64Decoder.decode(textBase64);

        XcMailrHelper.openHtmlContentWithCurrentWebDriver(textHtml);

        Assert.assertEquals(expectedText, $("body").getText() + "\n");
    }
}
