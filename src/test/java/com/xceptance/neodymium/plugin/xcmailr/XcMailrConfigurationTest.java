package com.xceptance.neodymium.plugin.xcmailr;

import java.util.UUID;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Assert;
import org.junit.Test;

public class XcMailrConfigurationTest extends AbstractTest
{
    @Test
    public void testApiToken()
    {
        final String apiToken = UUID.randomUUID().toString().replaceAll("-", "");
        properties.put("xcmailr.apiToken", apiToken);
        writeMapToPropertiesFile(properties, temporaryConfigurationFile);
        ConfigFactory.setProperty("xcmailr.temporaryConfigFile", "file:" + TEMPORARY_CONFIGURATION_FILE_LOCATION);
        Assert.assertEquals(apiToken, ConfigFactory.create(XcMailrConfiguration.class).apiToken());
    }

    @Test
    public void testUrl()
    {
        final String url = "https://www.xceptance.com/en/";
        properties.put("xcmailr.url", url);
        writeMapToPropertiesFile(properties, temporaryConfigurationFile);
        ConfigFactory.setProperty("xcmailr.temporaryConfigFile", "file:" + TEMPORARY_CONFIGURATION_FILE_LOCATION);
        Assert.assertEquals(url, ConfigFactory.create(XcMailrConfiguration.class).url());
    }

    @Test
    public void testTemporaryMailValidMinutes()
    {
        final String temporaryMailValidMinutes = "100";
        properties.put("xcmailr.temporaryMailValidMinutes", temporaryMailValidMinutes);
        writeMapToPropertiesFile(properties, temporaryConfigurationFile);
        ConfigFactory.setProperty("xcmailr.temporaryConfigFile", "file:" + TEMPORARY_CONFIGURATION_FILE_LOCATION);
        Assert.assertEquals(Integer.parseInt(temporaryMailValidMinutes),
                            ConfigFactory.create(XcMailrConfiguration.class).temporaryMailValidMinutes());
    }

    @Test
    public void testTemporaryMailValidMinutesDefault()
    {
        Assert.assertEquals(15, ConfigFactory.create(XcMailrConfiguration.class).temporaryMailValidMinutes());
    }

    @Test
    public void testMaximumWaitingMinutes()
    {
        final String maximumWaitingMinutes = "5";
        properties.put("xcmailr.maximumWaitingMinutes", maximumWaitingMinutes);
        writeMapToPropertiesFile(properties, temporaryConfigurationFile);
        ConfigFactory.setProperty("xcmailr.temporaryConfigFile", "file:" + TEMPORARY_CONFIGURATION_FILE_LOCATION);
        Assert.assertEquals(Integer.parseInt(maximumWaitingMinutes), ConfigFactory.create(XcMailrConfiguration.class).maximumWaitingTime());
    }

    @Test
    public void testMaximumWaitingMinutesDefault()
    {
        Assert.assertEquals(10, ConfigFactory.create(XcMailrConfiguration.class).maximumWaitingTime());
    }

    @Test
    public void testPollingIntervalSeconds()
    {
        final String pollingIntervalSeconds = "15";
        properties.put("xcmailr.pollingIntervalSeconds", pollingIntervalSeconds);
        writeMapToPropertiesFile(properties, temporaryConfigurationFile);
        ConfigFactory.setProperty("xcmailr.temporaryConfigFile", "file:" + TEMPORARY_CONFIGURATION_FILE_LOCATION);
        Assert.assertEquals(Integer.parseInt(pollingIntervalSeconds), ConfigFactory.create(XcMailrConfiguration.class).pollingInterval());
    }

    @Test
    public void testPollingIntervalSecondsDefault()
    {
        Assert.assertEquals(30, ConfigFactory.create(XcMailrConfiguration.class).pollingInterval());
    }
}
