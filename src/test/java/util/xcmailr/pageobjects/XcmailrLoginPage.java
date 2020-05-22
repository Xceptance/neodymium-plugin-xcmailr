package util.xcmailr.pageobjects;

import static com.codeborne.selenide.Selenide.$;

import org.aeonbits.owner.ConfigFactory;

import com.codeborne.selenide.Selenide;

import util.xcmailr.XcMailrConfiguration;

public class XcmailrLoginPage
{
    public XcmailrLoginPage login(String email, String password)
    {
        String url = ConfigFactory.create(XcMailrConfiguration.class).url();
        Selenide.open(url);
        Selenide.clearBrowserCookies();
        Selenide.open(url);
        $(".loginAccount").click();
        $("#inputLoginMail").sendKeys(email);
        $("#inputLoginPassword").sendKeys(password);
        $("#btnLoginSubmit").click();
        return this;
    }

    public XcmailrOverviewPage openMailOverview()
    {
        $(".mailOverview[href='/mail']").click();
        return new XcmailrOverviewPage();
    }
}
