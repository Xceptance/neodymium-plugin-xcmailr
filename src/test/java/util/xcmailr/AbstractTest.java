package util.xcmailr;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.Result;

import com.google.common.base.Joiner;

public abstract class AbstractTest
{
    // FIXME You should only require one temp file afaik, hence you don't need a list
    protected List<File> tempFiles = new LinkedList<>();

    protected Map<String, String> properties2 = new HashMap<>();

    protected final String fileLocation = "config/temp-xcmailr.properties";

    protected File tempConfigFile2 = new File("./" + fileLocation);

    @Before
    // FIXME naming
    public void setupApplitoolsConfiguration()
    {
        tempFiles.add(tempConfigFile2);
    }

    @After
    // FIXME rename to what is cleaned up
    public void cleanup()
    {
        for (final File tempFile : tempFiles)
        {
            deleteTempFile(tempFile);
        }
    }

    // FIXME only called in method that is not called?
    public void check(Result result, boolean expectedSuccessful, int expectedRunCount, int expectedIgnoreCount, int expectedFailCount,
                      String expectedFailureMessage)
    {
        Assert.assertEquals("Test successful", expectedSuccessful, result.wasSuccessful());
        Assert.assertEquals("Method run count", expectedRunCount, result.getRunCount());
        Assert.assertEquals("Method ignore count", expectedIgnoreCount, result.getIgnoreCount());
        Assert.assertEquals("Method fail count", expectedFailCount, result.getFailureCount());

        if (expectedFailureMessage != null)
        {
            Assert.assertTrue("Failure count", expectedFailCount == 1);
            Assert.assertEquals("Failure message", expectedFailureMessage, result.getFailures().get(0).getMessage());
        }
    }

    // FIXME never called?
    public void checkPass(Result result, int expectedRunCount, int expectedIgnoreCount, int expectedFailCount)
    {
        check(result, true, expectedRunCount, expectedIgnoreCount, expectedFailCount, null);
    }

    /**
     * delete a temporary test file
     */
    private static void deleteTempFile(File tempFile)
    {
        if (tempFile.exists())
        {
            try
            {
                Files.delete(tempFile.toPath());
            }
            catch (final Exception e)
            {
                // FIXME typo
                System.out.println(MessageFormat.format("Couldn''t delete temporary file: ''{0}'' caused by {1}",
                                                        tempFile.getAbsolutePath(), e));
            }
        }
    }

    public static void writeMapToPropertiesFile(Map<String, String> map, File file)
    {
        try
        {
            final String join = Joiner.on("\r\n").withKeyValueSeparator("=").join(map);

            final FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(join.getBytes());
            outputStream.close();
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
