package util.xcmailr;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.aeonbits.owner.ConfigFactory;
import org.junit.After;

public abstract class AbstractTest
{
    protected static final Map<String, String> properties = new HashMap<>();

    protected static final String TEMPORARY_CONFIGURATION_FILE_LOCATION = "config/temp-xcmailr.properties";

    protected static final File temporaryConfigurationFile = new File("./" + TEMPORARY_CONFIGURATION_FILE_LOCATION);

    @After
    public void deleteTempFile()
    {
        deleteTempFile(temporaryConfigurationFile);
        properties.clear();
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
                System.out.println(MessageFormat.format("Couldn''t delete temporary file: ''{0}'' caused by {1}",
                                                        tempFile.getAbsoluteFile(), e));
            }
        }
    }

    protected static void savePropertiesAndApply()
    {
        writeMapToPropertiesFile(properties, temporaryConfigurationFile);
        ConfigFactory.setProperty("xcmailr.temporaryConfigFile", "file:" + TEMPORARY_CONFIGURATION_FILE_LOCATION);
    }

    public static void writeMapToPropertiesFile(Map<String, String> map, File file)
    {
        try
        {
            String propertiesString = map.keySet().stream()
                                         .map(key -> key + "=" + map.get(key))
                                         .collect(Collectors.joining("\r\n"));

            final FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(propertiesString.getBytes());
            outputStream.close();
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
