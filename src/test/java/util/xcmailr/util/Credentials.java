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

    @Key("SMTP_SERVER_EMAIL")
    public String smtpServerEmail();

    @Key("SMTP_SERVER_PASSWORD")
    public String smtpServerPassword();

    @Key("SMTP_SERVER_LOGIN")
    public String smtpServerLogin();

    @Key("SMTP_SERVER_HOST")
    public String smtpServerHost();

    @Key("SMTP_SERVER_PORT")
    @DefaultValue("25")
    public int smtpServerPort();
}
