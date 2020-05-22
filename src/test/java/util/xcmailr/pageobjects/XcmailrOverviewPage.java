package util.xcmailr.pageobjects;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

public class XcmailrOverviewPage
{
    private ElementsCollection tempEmails = $$(".ng-scope>.mbAddress>.mailbox-adress");

    public XcmailrOverviewPage validateEmailCreated(String email)
    {
        tempEmails.findBy(exactText(email)).shouldBe(exist);
        return this;
    }

    public XcmailrOverviewPage validateEmailIsActive(String email)
    {
        findStatus(email).shouldNot(exist);
        return this;
    }

    public XcmailrOverviewPage validateEmailIsExpired(String email)
    {
        $$(".ng-scope.danger>.mbAddress>.ng-binding").findBy(exactText(email)).shouldBe(visible);
        return this;
    }

    public XcmailrOverviewPage refreshEmailsList()
    {
        $("#refresh-button").click();
        return this;
    }

    private SelenideElement findStatus(String email)
    {
        return tempEmails.findBy(exactText(email)).parent().parent().find(".mailbox-status-mailExpired");
    }

    public XcmailrOverviewPage deleteTempEmail(String email)
    {
        tempEmails.findBy(exactText(email)).parent().find(".glyphicon-trash").click();
        $(".btn-submit").click();
        return this;
    }
}
