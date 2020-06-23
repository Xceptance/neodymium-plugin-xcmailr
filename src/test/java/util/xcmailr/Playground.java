package util.xcmailr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import util.xcmailr.util.Credentials;
import util.xcmailr.util.SendRequest;

public class Playground extends AbstractTest
{
    private static final Credentials CREDENTIALS = ConfigFactory.create(Credentials.class, System.getenv());

    private static CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    @Test
    public void test()
    {
        // Request in browser

        // POST /login HTTP/1.1
        // Host: xcmailr.xceptance.de
        // Connection: keep-alive
        // Content-Length: 64
        // Cache-Control: max-age=0
        // Upgrade-Insecure-Requests: 1
        // Origin: https://xcmailr.xceptance.de
        // Content-Type: application/x-www-form-urlencoded
        // User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97
        // Safari/537.36
        // Accept:
        // text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9
        // Sec-Fetch-Site: same-origin
        // Sec-Fetch-Mode: navigate
        // Sec-Fetch-User: ?1
        // Sec-Fetch-Dest: document
        // Referer: https://xcmailr.xceptance.de/login
        // Accept-Encoding: gzip, deflate, br
        // Accept-Language: en-US,en;q=0.9,ru;q=0.8,de;q=0.7,ro;q=0.6
        // Cookie:
        // experimentation_subject_id=ImI5MWM0MjUzLWZkNjQtNGY0NS04M2YwLTM1ZjBkZDE0YTBhNiI%3D--bea83d4a8b58fb9ed6f3d515813789ea6248b476;
        // XCMailr_LANG=en;
        // XCMailr_SESSION="81d41d2afd3f311d0aad00813261f1dbcf77af9a-___TS=1592906462760&___ID=96c7321f-9c56-4bd5-bbff-024d4b5bc0d8"
        // Data: mail=o.omelianchuk%40xceptance.net&password=myPassword
        //

        login(CREDENTIALS.xcmailrEmail(), CREDENTIALS.xcmailrPassword());

        // following request uses apache http
        // returns dashboard html page in body
        // but Set-Cookie header has a form XCMailr_FLASH=;Path=/;Expires=Thu, 01-Jan-1970 00:00:00 GMT
        // which doesn't correspond with usual form in browser
        // all through the request seems to reach its purpose
        loginViaApache(CREDENTIALS.xcmailrEmail(), CREDENTIALS.xcmailrPassword());
    }

    public static void login(String xcmailrEmail, String xcmailrPassword)
    {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("mail", xcmailrEmail)
                                                             .addFormDataPart("password", xcmailrPassword).build();
        Request request = new Request.Builder().url(StringUtils.appendIfMissing(XcMailrApi.getConfiguration().url(), "/") + "/login")
                                               .addHeader("Accept-Language", "en-US").post(requestBody).build();

        try
        {
            Response response = client.newCall(request).execute();

            // returns html with <title>Oops. That&#39;s an internal server error and all we know.</title>
            System.out.println(response.body().string());
            // usually has a form like
            // XCMailr_SESSION="8e6ebb43cab761162311ae372cf42a1c1c2b0fe5-___TS=1592901888233&___ID=f5682929-8b22-4400-a78f-fea8312f0e01&o.omelianchuk%40xceptance.net"
            // but this response contains
            // XCMailr_SESSION="8e6ebb43cab761162311ae372cf42a1c1c2b0fe5-___TS=1592901888233&___ID=f5682929-8b22-4400-a78f-fea8312f0e01"
            System.out.println(response.header("Set-Cookie"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void loginViaApache(String xcmailrEmail, String xcmailrPassword)
    {
        try
        {
            final HttpPost postRequest = new HttpPost(StringUtils.appendIfMissing(XcMailrApi.getConfiguration().url(), "/") + "/login");
            postRequest.addHeader("Accept-Language", "en-US");

            // add form parameters:
            final List<BasicNameValuePair> formparams = new ArrayList<>();
            formparams.add(new BasicNameValuePair("mail", xcmailrEmail));
            formparams.add(new BasicNameValuePair("password", xcmailrPassword));

            // encode form parameters and add
            final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams);
            postRequest.setEntity(entity);

            final HttpResponse response = httpClient.execute(postRequest);
            System.out.println(SendRequest.readResponse(response.getEntity().getContent()).toString());
            for (Header header : response.getAllHeaders())
            {
                System.out.println(header.getName() + " : " + header.getValue());
            }
            postRequest.releaseConnection();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
