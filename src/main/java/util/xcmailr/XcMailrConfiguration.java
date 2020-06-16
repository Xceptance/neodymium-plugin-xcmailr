package util.xcmailr;

import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Mutable;

@LoadPolicy(LoadType.MERGE)
@Sources(
    {
        "${xcmailr.temporaryConfigFile}", "file:config/dev-xcmailr.properties", "file:config/xcmailr.properties"
    })

public interface XcMailrConfiguration extends Mutable
{
    @Key("xcmailr.url")
    @DefaultValue("https://xcmailr.xceptance.de")
    public String url();

    @Key("xcmailr.apiToken")
    public String apiToken();

    @Key("xcmailr.temporaryMailValidMinutes")
    @DefaultValue("15")
    public int temporaryMailValidMinutes();

    @Key("xcmailr.maximumWaitingMinutes")
    @DefaultValue("10")
    public int maximumWaitingTime();

    @Key("xcmailr.pollingIntervalSeconds")
    @DefaultValue("30")
    public int pollingInterval();
}
