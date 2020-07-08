package util.xcmailr.util;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.JsonPath;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import util.xcmailr.XcMailrApi;

public class SendRequest
{
    private static OkHttpClient client = new OkHttpClient.Builder().cookieJar(new CookieSession()).build();

    public static void login(String xcmailrEmail, String xcmailrPassword)
    {
        RequestBody requestBody = new FormBody.Builder().add("mail", xcmailrEmail).add("password", xcmailrPassword).build();
        Request request = new Request.Builder().url(StringUtils.appendIfMissing(XcMailrApi.getConfiguration().url(), "/") + "/login")
                                               .addHeader("Accept-Language", "en-US").post(requestBody).build();

        try
        {
            client.newCall(request).execute();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static JsonObject getTempEmail(String tempEmail)
    {
        Request request = new Request.Builder().url(StringUtils.appendIfMissing(XcMailrApi.getConfiguration().url(), "/") + "mail/getmails")
                                               .build();

        try
        {
            Response response = client.newCall(request).execute();
            final String sb = response.body().string();
            final String emails = JsonPath.read(sb.toString(), "$[?(@.fullAddress=='" + tempEmail + "')]").toString();
            final JsonArray emailObject = new JsonParser().parse(emails).getAsJsonArray();
            if (emailObject.size() > 0)
            {
                return emailObject.get(0).getAsJsonObject();
            }
            return null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean emailExists(String email)
    {
        return getTempEmail(email) != null;
    }

    public static boolean emailExpired(String email)
    {
        return getTempEmail(email).get("expired").getAsBoolean();
    }

    public static void deleteTempEmail(String tempEmail)
    {
        String id = getTempEmail(tempEmail).get("id").getAsString();
        RequestBody reqbody = RequestBody.create(null, new byte[0]);
        Request request = new Request.Builder().url(StringUtils.appendIfMissing(XcMailrApi.getConfiguration().url(), "/") + "mail/delete/" +
                                                    id)
                                               .method("POST", reqbody).header("Content-Length", "0").build();
        try
        {
            client.newCall(request).execute();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
