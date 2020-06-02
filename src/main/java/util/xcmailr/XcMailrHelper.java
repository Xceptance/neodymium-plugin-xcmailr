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
     * Retrieves the text content of the first e-mail from the JSON response body. *
     * 
     * @param responseBody
     *            is the return value of XcMailApi.fetchEmails() and its sugar methods
     * @return a String containing the e-mail's text content
     */
    public static String getFirstMailsTextContent(String responseBody)
    {
        return getFieldOfMailObject(responseBody, "textContent");
    }

    /**
     * Retrieves the HTML content of the first e-mail from the JSON response body. *
     * 
     * @param responseBody
     *            is the return value of XcMailApi.fetchEmails() and its sugar methods
     * @return a String containing the e-mail's HTML content
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

        JsonArray emailArray = tempJsonElement.getAsJsonArray();
        tempJsonElement = emailArray.get(0);
        final JsonObject emailObject = tempJsonElement.getAsJsonObject();
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
     * Open the supplied HTML content with the current web driver.
     * 
     * @param htmlContent
     *            is any HTML text, e.g. return value of the getFirstMailsHtmlContent() method
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
