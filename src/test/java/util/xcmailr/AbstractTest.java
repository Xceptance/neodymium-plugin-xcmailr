package util.xcmailr;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.aeonbits.owner.ConfigFactory;
import org.junit.After;

import com.google.common.base.Joiner;

public abstract class AbstractTest
{
    protected static Map<String, String> properties = new HashMap<>();

    protected static final String fileLocation = "config/temp-xcmailr.properties";

    protected static File tempConfigFile2 = new File("./" + fileLocation);

    @After
    public void deleteTempFile()
    {
        deleteTempFile(tempConfigFile2);
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
                // double apostrophe needed, otherwise MessageFormat.format() won't work
                System.out.println(MessageFormat.format(
                                                        "Coundn''t delete temporary file: ''{0}'' caused by {1}",
                                                        tempFile.getAbsoluteFile(), e));
            }
        }
    }

    protected static void writeProperty()
    {
        writeMapToPropertiesFile(properties, tempConfigFile2);
        ConfigFactory.setProperty("xcmailr.temporaryConfigFile", "file:" + fileLocation);
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
