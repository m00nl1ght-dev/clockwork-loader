package dev.m00nl1ght.clockwork.util;

public class LogUtil {

    public static String format(String str, String tok, Object... objects) {
        var p = 0;
        for (var object : objects) {
            if (p >= str.length()) break;
            p = str.indexOf(tok, p);
            if (p < 0) break;
            final var os = object.toString();
            str = str.substring(0, p + 1) + os + str.substring(p + 2);
            p += os.length() + 2;
        }
        return str;
    }

}
