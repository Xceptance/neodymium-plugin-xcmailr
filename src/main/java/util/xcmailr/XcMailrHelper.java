package util.xcmailr;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;

import org.junit.Assert;

import com.codeborne.selenide.Selenide;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class XcMailrHelper
{
    /**
     * Retrieve messages's text content from json response body
     * 
     * @param responseBody
     * @return messages's text content
     */
    public static String getFirstMailsTextContent(String responseBody)
    {
        return getFieldOfMailObject(responseBody, "textContent");
    }

    /**
     * Retrieve messages's html content from json response body
     * 
     * @param responseBody
     * @return messages's html content
     */
    public static String getFirstMailsHtmlContent(String responseBody)
    {
        return getFieldOfMailObject(responseBody, "htmlContent");
    }

    private static String getFieldOfMailObject(String responseBody, String fieldName)
    {
        Assert.assertNotNull(responseBody);
        final JsonParser parser = new JsonParser();
        JsonElement tempJsonElement = parser.parse(responseBody);

        JsonObject emailObject = null;

        if (tempJsonElement.isJsonArray())
        {
            JsonArray emailArray = tempJsonElement.getAsJsonArray();
            if (emailArray.size() == 1)
            {
                tempJsonElement = emailArray.get(0);
                if (tempJsonElement.isJsonObject())
                {
                    emailObject = tempJsonElement.getAsJsonObject();
                }
            }
        }
        Assert.assertNotNull(emailObject);

        if (emailObject.has(fieldName))
        {
            final String encodedString = emailObject.get(fieldName).getAsString();

            final byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
            final String decodedString = new String(decodedBytes);

            return decodedString;
        }
        return null;
    }

    /**
     * Open passed html content in current browser window
     * 
     * @param htmlContent
     */
    public static void openHtmlContentWithCurrentWebDriver(String htmlContent)
    {
        File tempHtmlContentFile = null;
        try
        {
            tempHtmlContentFile = File.createTempFile("htmlContent", ".html", new File("./target/"));
            tempHtmlContentFile.deleteOnExit();

            FileWriter fileWriter;
            fileWriter = new FileWriter(tempHtmlContentFile);
            fileWriter.append(htmlContent);
            fileWriter.close();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        Selenide.open("file://" + tempHtmlContentFile.getAbsolutePath());
    }
}
