package util.xcmailr.util;

import java.io.BufferedReader;
import java.io.IOException;
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

import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class SendRequest
{
    private static CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    public static void login(String email, String password)
    {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try
        {
            HttpPost postRequest = new HttpPost("https://xcmailr.xceptance.de/login");
            postRequest.addHeader("Accept-Language", "en-US");

            // add form parameters:
            List<BasicNameValuePair> formparams = new ArrayList<>();
            formparams.add(new BasicNameValuePair("mail", email));
            formparams.add(new BasicNameValuePair("password", password));

            // encode form parameters and add
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams);
            postRequest.setEntity(entity);

            HttpResponse response;

            response = httpClient.execute(postRequest);

            br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

            String output = br.readLine();
            while (output != null)
            {
                sb.append(output);
                output = br.readLine();
            }
            postRequest.releaseConnection();
        }
        catch (IOException e)
        {
            try
            {
                br.close();
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static JSONObject getTempEmail(String tempEmail)
    {
        HttpResponse response = null;
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try
        {
            HttpGet getRequest = new HttpGet("https://xcmailr.xceptance.de/mail/getmails");

            response = httpClient.execute(getRequest);
            br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

            String output = br.readLine();
            while (output != null)
            {
                sb.append(output);
                output = br.readLine();
            }
            getRequest.releaseConnection();
            JSONArray emails = (JSONArray) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(sb.toString());
            JSONArray emailObject = (JSONArray) JsonPath.read(emails, "$[?(@.fullAddress=='" + tempEmail + "')]");

            if (emailObject.size() > 0)
            {
                return (JSONObject) emailObject.get(0);
            }
            return null;
        }
        catch (IOException | ParseException e)
        {
            try
            {
                br.close();
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static boolean emailExists(String email)
    {
        return getTempEmail(email) != null;
    }

    public static boolean emailExpired(String email)
    {
        return (Boolean) getTempEmail(email).get("expired");
    }

    public static void deleteTempEmail(String tempEmail)
    {
        String id = ((Long) getTempEmail(tempEmail).get("id")).toString();
        HttpResponse response = null;
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try
        {
            HttpPost postRequest = new HttpPost("https://xcmailr.xceptance.de/mail/delete/" + id);
            response = httpClient.execute(postRequest);

            br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            String output = br.readLine();
            while (output != null)
            {
                sb.append(output);
                output = br.readLine();
            }
            postRequest.releaseConnection();
        }
        catch (IOException e)
        {
            try
            {
                br.close();
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}