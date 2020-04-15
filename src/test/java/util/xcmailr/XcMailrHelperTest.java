package util.xcmailr;

import static com.codeborne.selenide.Selenide.$;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.xceptance.neodymium.NeodymiumRunner;
import com.xceptance.neodymium.module.statement.browser.multibrowser.Browser;

import gherkin.deps.net.iharder.Base64;

@RunWith(NeodymiumRunner.class)
public class XcMailrHelperTest extends AbstractTest
{
    private String textBase64 = "SGkNCg0KSG93IGFyZSB5b3U/KQ0KDQpCeWUNCg==";

    private String textHtmlBase64 = "PGRpdiBkaXI9ImF1dG8iPkhpPGRpdiBkaXI9ImF1dG8iPjxicj48L2Rpdj48ZGl2IGRpcj0iYXV0byI+SG93IGFyZSB5b3U/KTwvZGl2PjxkaXYgZGlyPSJhdXRvIj48YnI+PC9kaXY+PGRpdiBkaXI9ImF1dG8iPkJ5ZTwvZGl2PjwvZGl2Pg0K";

    private String response = "[{'mailAddress':'foo.bar@de.com',"
                              + "'sender':'foo@bar.com',"
                              + "'subject':'test',"
                              + "'receivedTime':1586959433701,"
                              + "'attachments':[],"
                              + "'downloadToken':'8f737e8c-8df3-41ab-a3f2-50da4542244b',"
                              + "'htmlContent':'" + textHtmlBase64 + "',"
                              + "'textContent':'" + textBase64 + "'}]";

    @Test
    public void testGetFirstMailsTextContent() throws IOException
    {
        String text = new String(Base64.decode(textBase64));

        Assert.assertEquals(text, XcMailrHelper.getFirstMailsTextContent(response));
    }

    @Test
    public void testGetFirstMailsHmlContent() throws IOException
    {
        String textHtml = new String(Base64.decode(textHtmlBase64));

        Assert.assertEquals(textHtml, XcMailrHelper.getFirstMailsHtmlContent(response));
    }

    @Browser("Chrome_headless")
    @Test
    public void testOpenHtmlContentWithCurrentWebDriver() throws IOException
    {
        String textHtml = new String(Base64.decode(textHtmlBase64));

        char cr = 13;
        String expectedText = (new String(Base64.decode(textBase64))).replaceAll(String.valueOf(cr), "");

        XcMailrHelper.openHtmlContentWithCurrentWebDriver(textHtml);

        Assert.assertEquals(expectedText, $("body").getText() + "\n");
    }
}
