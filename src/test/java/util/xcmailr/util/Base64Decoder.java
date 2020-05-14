package util.xcmailr.util;

import org.aeonbits.owner.util.Base64;

// TODO rename class so it highlights that it is a helper
public class Base64Decoder
{
    // TODO add comment to explain what this method does
    // FIXME rename method so it describes that it decodes and replaces CR
    public static String decode(String encodedText)
    {
        // FIXME make one line
        final char cr = 13;
        return (new String(Base64.decode(encodedText))).replaceAll(String.valueOf(cr), "");
    }
}
