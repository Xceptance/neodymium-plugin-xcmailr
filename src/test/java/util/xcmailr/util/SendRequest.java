package util.xcmailr.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.JsonPath;

import util.xcmailr.XcMailrApi;

public class SendRequest
{
    private static CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    public static void login(String email, String password)
    {
        try
        {
            final HttpPost postRequest = new HttpPost(XcMailrApi.getConfiguration().url() + "/login");
            postRequest.addHeader("Accept-Language", "en-US");

            // add form parameters:
            final List<BasicNameValuePair> formparams = new ArrayList<>();
            formparams.add(new BasicNameValuePair("mail", email));
            formparams.add(new BasicNameValuePair("password", password));

            // encode form parameters and add
            final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams);
            postRequest.setEntity(entity);

            httpClient.execute(postRequest);

            postRequest.releaseConnection();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static JsonObject getTempEmail(String tempEmail)
    {
        try
        {
            final HttpGet getRequest = new HttpGet(XcMailrApi.getConfiguration().url() + "/mail/getmails");

            final HttpResponse response = httpClient.execute(getRequest);

            final StringBuilder sb = readResponse(response.getEntity().getContent());
            getRequest.releaseConnection();
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
            throw new RuntimeException(e);
        }
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
        try
        {
            final HttpPost postRequest = new HttpPost(XcMailrApi.getConfiguration().url() + "/mail/delete/" + id);
            httpClient.execute(postRequest);

            postRequest.releaseConnection();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static StringBuilder readResponse(InputStream inputStream)
    {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try
        {
            br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            sb = new StringBuilder();
            for (String output = br.readLine(); output != null; output = br.readLine())
            {
                sb.append(output);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("couldn't read the response from server", e);
        }
        finally
        {
            try
            {
                br.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException("error while closing the buffered readed", e);
            }
        }
        return sb;
    }
}