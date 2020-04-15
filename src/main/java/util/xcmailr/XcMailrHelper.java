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

    public static String getFirstMailsTextContent(String resonseBody)
    {
        return getFieldOfMailObject(resonseBody, 0, "textContent");
    }

    public static String getFirstMailsHtmlContent(String responseBody)
    {
        return getFieldOfMailObject(responseBody, 0, "htmlContent");
    }

    private static String getFieldOfMailObject(String resonseBody, int emailNo, String fieldName)
    {
        Assert.assertNotNull(resonseBody);
        final JsonParser parser = new JsonParser();
        JsonElement tempJsonElement = parser.parse(resonseBody);
        JsonArray emailArray;
        JsonObject emailObject = null;

        if (tempJsonElement.isJsonArray())
        {
            emailArray = tempJsonElement.getAsJsonArray();
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
            String encodedString = emailObject.get(fieldName).getAsString();

            byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
            String decodedString = new String(decodedBytes);

            return decodedString;
        }
        return null;
    }

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
        catch (IOException e)
        {
            e.printStackTrace();
        }
        Selenide.open("file://" + tempHtmlContentFile.getAbsolutePath());
        Selenide.sleep(4000);

    }
}
