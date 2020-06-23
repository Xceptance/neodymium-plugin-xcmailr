package util.xcmailr;

import java.io.IOException;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import util.xcmailr.util.Credentials;

public class Playground extends AbstractTest
{
    private static final Credentials CREDENTIALS = ConfigFactory.create(Credentials.class, System.getenv());

    @Test
    public void test()
    {
        login(CREDENTIALS.xcmailrEmail(), CREDENTIALS.xcmailrPassword());

        // following request uses apache http
        // returns dashboard html page in body
        // but Set-Cookie header has a form XCMailr_FLASH=;Path=/;Expires=Thu, 01-Jan-1970 00:00:00 GMT
        // which doesn't correspond with usual form in browser
        // all through the request seems to reach its purpose
        // SendRequest.login(CREDENTIALS.xcmailrEmail(), CREDENTIALS.xcmailrPassword());
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
}
