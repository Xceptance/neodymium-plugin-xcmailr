package util.xcmailr.pageobjects;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import com.codeborne.selenide.ElementsCollection;

public class XcmailrOverviewPage
{
    private ElementsCollection tempEmails = $$(".ng-scope>.mbAddress>.mailbox-adress");

    public XcmailrOverviewPage validateEmailCreated(String email)
    {
        tempEmails.findBy(exactText(email)).shouldBe(visible);
        return this;
    }

    public XcmailrOverviewPage validateEmailIsActive(String email)
    {
        tempEmails.findBy(exactText(email)).parent().parent().find(".mailbox-status-mailExpired").shouldNot(exist);
        return this;
    }

    public XcmailrOverviewPage validateEmailIsExpired(String email)
    {
        tempEmails.findBy(exactText(email)).parent().parent().find(".mailbox-status-mailExpired").shouldBe(visible);
        return this;
    }

    public XcmailrOverviewPage deleteTempEmail(String email)
    {
        tempEmails.findBy(exactText(email)).parent().find(".glyphicon-trash").click();
        $(".btn-submit").click();
        return this;
    }
}
