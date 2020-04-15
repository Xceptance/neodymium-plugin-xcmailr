package util.xcmailr;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.codeborne.selenide.Selenide;
import com.xceptance.neodymium.NeodymiumRunner;
import com.xceptance.neodymium.module.statement.browser.multibrowser.Browser;

@RunWith(NeodymiumRunner.class)
@Browser("Chrome_1500x1000")
public class XcMailrApiTest
{
    @Test
    public void test()
    {
        Selenide.open("https://www.xceptance.com/en/");
        // XcMailrApi.createTemporaryEmail("testTest1@varmail.de");
        String response = XcMailrApi.retrieveLastEmailBySubject("testTest@varmail.de",
                                                                "test");
        System.out.println(response);
        String textHtml = XcMailrHelper.getFirstMailsHtmlContent(response);
        System.out.println(textHtml);
        String text = XcMailrHelper.getFirstMailsTextContent(response);
        System.out.println(text);
        XcMailrHelper.openHtmlContentWithCurrentWebDriver(text);
    }
}
