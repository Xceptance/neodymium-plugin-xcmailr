package util.xcmailr;

import java.util.Calendar;
import java.util.Date;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;

import xcmailr.client.Mailbox;

public class EmailCreationApiTest extends AbstractXcMailrApiTest
{
    @Test
    public void testEmailCreated()
    {
        XcMailrApi.createTemporaryEmail(tempEmail, false);
        Assert.assertNotNull(XcMailrApi.getMailbox(tempEmail));
    }

    @Test
    public void testListMailboxes()
    {
        Mailbox createdMailbox = XcMailrApi.createTemporaryEmail(tempEmail, false);
        Assert.assertEquals(1, XcMailrApi.listMailboxes().stream().filter(mailbox -> mailbox.address.equals(createdMailbox.address))
                                         .collect(Collectors.toList()).size());
    }

    @Test
    public void testEmailExpired()
    {
        Mailbox createdMailbox = XcMailrApi.createTemporaryEmail(tempEmail, false);

        Assert.assertEquals(DateUtils.round(DateUtils.addMinutes(new Date(), XcMailrApi.getConfiguration().temporaryMailValidMinutes()),
                                            Calendar.MINUTE),
                            DateUtils.round(new Date(createdMailbox.deactivationTime), Calendar.MINUTE));
    }

    @Test
    public void testDeleteMailbox()
    {
        XcMailrApi.createTemporaryEmail(tempEmail, false);
        XcMailrApi.deleteMailbox(tempEmail);
        Assert.assertNull(XcMailrApi.getMailbox(tempEmail));
    }

    @Test
    public void testUpdateMailbox()
    {
        XcMailrApi.createTemporaryEmail(tempEmail, false);
        final String newAddress = randomEmail("test", "xcmailr.test");
        final int validMinutes = 2;
        Date deactivationTime = DateUtils.addMinutes(new Date(), validMinutes);
        final boolean forward = true;

        XcMailrApi.updateMailbox(tempEmail, newAddress, validMinutes, forward);
        tempEmail = newAddress;

        Mailbox newMailbox = XcMailrApi.getMailbox(newAddress);
        validateMailbox(new Mailbox(newAddress, deactivationTime.getTime(), forward), newMailbox);
    }

    @Test
    public void testUpdateMailboxAddress()
    {
        Mailbox oldMailbox = XcMailrApi.createTemporaryEmail(tempEmail, false);
        final String newAddress = randomEmail("test", "xcmailr.test");

        XcMailrApi.updateMailboxAddress(tempEmail, newAddress);
        tempEmail = newAddress;

        Mailbox newMailbox = XcMailrApi.getMailbox(newAddress);
        validateMailbox(new Mailbox(newAddress, oldMailbox.deactivationTime, false), newMailbox);
    }

    @Test
    public void testUpdateMailboxValidMinutes()
    {
        XcMailrApi.createTemporaryEmail(tempEmail, false);
        final int validMinutes = 2;
        Date deactivationTime = DateUtils.addMinutes(new Date(), validMinutes);
        XcMailrApi.updateMailboxValidMinutes(tempEmail, validMinutes);

        Mailbox newMailbox = XcMailrApi.getMailbox(tempEmail);
        validateMailbox(new Mailbox(tempEmail, deactivationTime.getTime(), false), newMailbox);
    }

    @Test
    public void testUpdateMailboxForwarding()
    {
        Mailbox oldMailbox = XcMailrApi.createTemporaryEmail(tempEmail, false);
        final boolean forward = true;

        XcMailrApi.updateMailboxForwarding(tempEmail, forward);

        Mailbox newMailbox = XcMailrApi.getMailbox(tempEmail);
        validateMailbox(new Mailbox(tempEmail, oldMailbox.deactivationTime, forward), newMailbox);
    }

    private void validateMailbox(Mailbox expected, Mailbox actuals)
    {
        Assert.assertEquals(expected.address, actuals.address);
        Assert.assertEquals(DateUtils.round(new Date(expected.deactivationTime), Calendar.MINUTE),
                            DateUtils.round(new Date(actuals.deactivationTime), Calendar.MINUTE));
        Assert.assertEquals(expected.forwardEnabled, actuals.forwardEnabled);
    }
}
