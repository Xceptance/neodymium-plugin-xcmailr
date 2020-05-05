package util.xcmailr.pageobjects;

import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Selenide;

public class XcmailrLoginPage
{
    public XcmailrLoginPage login(String email, String password)
    {
        Selenide.open("https://xcmailr.xceptance.de/");
        Selenide.clearBrowserCookies();
        Selenide.open("https://xcmailr.xceptance.de/");
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
