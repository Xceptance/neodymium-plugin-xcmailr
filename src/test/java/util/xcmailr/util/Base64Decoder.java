package util.xcmailr.util;

import org.aeonbits.owner.util.Base64;

public class Base64Decoder
{
    public static String decode(String encodedText)
    {
        char cr = 13;
        return (new String(Base64.decode(encodedText))).replaceAll(String.valueOf(cr), "");
    }
}
