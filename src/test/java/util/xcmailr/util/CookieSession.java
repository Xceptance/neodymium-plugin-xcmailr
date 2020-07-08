package util.xcmailr.util;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class CookieSession implements CookieJar
{

    private List<Cookie> cookies = new ArrayList<Cookie>();

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies)
    {
        this.cookies.addAll(cookies);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url)
    {
        if (cookies != null)
        {
            return cookies;
        }
        return new ArrayList<Cookie>();

    }
}
