package com.xceptance.neodymium.plugin.xcmailr;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;

import xcmailr.client.Mailbox;

public class EmailCreationApiTest extends AbstractXcMailrApiTest
{
    public static final int COMPUTATIONAL_DIFFERENCE = 1000;

    @Test
    public void testEmailCreated()
    {
        Mailbox createdMailbox = XcMailrApi.createTemporaryEmail(emailUnderTest, false);
        Mailbox fetchedMailbox = XcMailrApi.getMailbox(emailUnderTest);

        Assert.assertNotNull(createdMailbox);
        Assert.assertNotNull(fetchedMailbox);
        assertMailboxEquality(createdMailbox, fetchedMailbox);
    }

    @Test
    public void testListMailboxes()
    {
        Mailbox createdMailbox = XcMailrApi.createTemporaryEmail(emailUnderTest, false);
        List<Mailbox> mailboxes = XcMailrApi.listMailboxes();
        Assert.assertEquals(1, mailboxes.stream().filter(mailbox -> mailbox.address.equals(createdMailbox.address))
                                        .collect(Collectors.toList()).size());
    }

    @Test
    public void testMailboxDeactivationTime()
    {
        Date creationDate = new Date();
        Mailbox createdMailbox = XcMailrApi.createTemporaryEmail(emailUnderTest, false);
        Assert.assertNotNull(createdMailbox);

        long deactivation = DateUtils.addMinutes(creationDate, XcMailrApi.getConfiguration().temporaryMailValidMinutes()).getTime();
        timeIsValidWithComputationalDiffernce(deactivation, createdMailbox.deactivationTime);
    }

    @Test
    public void testDeleteMailbox()
    {
        Mailbox createdMailbox = XcMailrApi.createTemporaryEmail(emailUnderTest, false);
        Assert.assertNotNull(createdMailbox);

        XcMailrApi.deleteMailbox(emailUnderTest);
        Assert.assertNull(XcMailrApi.getMailbox(emailUnderTest));
    }

    @Test
    public void testUpdateMailboxComplete()
    {
        Mailbox createdMailbox = XcMailrApi.createTemporaryEmail(emailUnderTest, false);
        Assert.assertNotNull(createdMailbox);

        final String newEmail = randomEmail(EMAIL_PREFIX, EMAIL_SUFFIX);
        Assert.assertNotEquals(emailUnderTest, newEmail);

        final int validMinutes = 2;
        Date deactivationTime = DateUtils.addMinutes(new Date(), validMinutes);
        final boolean forward = true;

        Mailbox updatedMailbox = XcMailrApi.updateMailbox(emailUnderTest, newEmail, validMinutes, forward);
        emailUnderTest = newEmail;
        assertMailboxEqualityAfterUpdate(new Mailbox(emailUnderTest, deactivationTime.getTime(), forward), updatedMailbox);

        Mailbox retrievedMailbox = XcMailrApi.getMailbox(newEmail);
        assertMailboxEquality(updatedMailbox, retrievedMailbox);
    }

    @Test
    public void testUpdateMailboxOnlyEmail()
    {
        Mailbox createdMailbox = XcMailrApi.createTemporaryEmail(emailUnderTest, false);
        Assert.assertNotNull(createdMailbox);

        final String newEmail = randomEmail(EMAIL_PREFIX, EMAIL_SUFFIX);
        Assert.assertNotEquals(emailUnderTest, newEmail);

        Mailbox updatedMailbox = XcMailrApi.updateMailboxEmailAddress(emailUnderTest, newEmail);
        emailUnderTest = newEmail;
        assertMailboxEqualityAfterUpdate(new Mailbox(emailUnderTest, createdMailbox.deactivationTime, false), updatedMailbox);

        Mailbox retrievedMailbox = XcMailrApi.getMailbox(newEmail);
        assertMailboxEquality(updatedMailbox, retrievedMailbox);
    }

    @Test
    public void testUpdateMailboxOnlyDeactivationTime()
    {
        Mailbox createdMailbox = XcMailrApi.createTemporaryEmail(emailUnderTest, false);
        Assert.assertNotNull(createdMailbox);

        final int validMinutes = 2;
        Date deactivationTime = DateUtils.addMinutes(new Date(), validMinutes);
        Mailbox updatedMailbox = XcMailrApi.updateMailboxDeactivationTime(emailUnderTest, validMinutes);
        assertMailboxEqualityAfterUpdate(new Mailbox(emailUnderTest, deactivationTime.getTime(), false), updatedMailbox);

        Mailbox newMailbox = XcMailrApi.getMailbox(emailUnderTest);
        assertMailboxEquality(updatedMailbox, newMailbox);
    }

    @Test
    public void testUpdateMailboxOnlyForwarding()
    {
        Mailbox createdMailbox = XcMailrApi.createTemporaryEmail(emailUnderTest, false);
        Assert.assertNotNull(createdMailbox);

        final boolean forward = true;
        Mailbox updatedMailbox = XcMailrApi.updateMailboxForwarding(emailUnderTest, forward);
        assertMailboxEqualityAfterUpdate(new Mailbox(emailUnderTest, createdMailbox.deactivationTime, forward), updatedMailbox);

        Mailbox retrievedMailbox = XcMailrApi.getMailbox(emailUnderTest);
        assertMailboxEquality(updatedMailbox, retrievedMailbox);
    }

    private void assertMailboxEqualityWithoutDeactivationTime(Mailbox expectedMB, Mailbox actualMB)
    {
        Assert.assertEquals(expectedMB.address, actualMB.address);
        Assert.assertEquals(expectedMB.forwardEnabled, actualMB.forwardEnabled);
    }

    private void assertMailboxEquality(Mailbox expectedMB, Mailbox actualMB)
    {
        assertMailboxEqualityWithoutDeactivationTime(expectedMB, actualMB);
        Assert.assertEquals(expectedMB.deactivationTime, actualMB.deactivationTime);
    }

    private void assertMailboxEqualityAfterUpdate(Mailbox expectedMB, Mailbox actualMB)
    {
        assertMailboxEqualityWithoutDeactivationTime(expectedMB, actualMB);
        timeIsValidWithComputationalDiffernce(expectedMB.deactivationTime, actualMB.deactivationTime);
    }

    private void timeIsValidWithComputationalDiffernce(long deactivation, long deactivationTime)
    {
        Assert.assertTrue(deactivation <= deactivationTime);
        Assert.assertTrue(deactivationTime < deactivation + COMPUTATIONAL_DIFFERENCE);
    }
}
