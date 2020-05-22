package util.xcmailr.util;

import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Mutable;

@LoadPolicy(LoadType.MERGE)
@Sources(
{
  "file:config/dev-credentials.properties", "file:config/credentials.properties"
})

public interface Credentials extends Mutable
{
    @Key("XCMAILR_EMAIL")
    public String xcmailrEmail();

    @Key("XCMAILR_PASSWORD")
    public String xcmailrPassword();

    @Key("EMAIL")
    public String senderEmail();

    @Key("EMAIL_PASSWORD")
    public String senderPassword();

    @Key("EMAIL_LOGIN")
    public String senderLogin();

    @Key("EMAIL_SERVER")
    public String senderServer();

    @Key("SENDER_PORT")
    @DefaultValue("25")
    public int senderPort();
}
