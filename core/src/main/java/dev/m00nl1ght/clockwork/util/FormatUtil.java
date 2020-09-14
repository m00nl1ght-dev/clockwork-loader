package dev.m00nl1ght.clockwork.util;

public class FormatUtil {

    private FormatUtil() {}

    public static String format(String str, Object... objects) {
        var p = 0;
        for (var object : objects) {
            if (p >= str.length()) break;
            p = str.indexOf("[]", p);
            if (p < 0) break;
            final var os = object.toString();
            str = str.substring(0, p + 1) + os + str.substring(p + 1);
            p += os.length() + 2;
        }
        return str;
    }

    public static RuntimeException rtExc(String msg, Object... objects) {
        return new RuntimeException(format(msg, objects));
    }

    public static RuntimeException rtExc(Throwable t, String msg, Object... objects) {
        return new RuntimeException(format(msg, objects), t);
    }

    public static IllegalArgumentException illArgExc(String msg, Object... objects) {
        return new IllegalArgumentException(format(msg, objects));
    }

    public static IllegalStateException illStateExc(String msg, Object... objects) {
        return new IllegalStateException(format(msg, objects));
    }

    public static UnsupportedOperationException unspExc(String msg, Object... objects) {
        return new UnsupportedOperationException(format(msg, objects));
    }

}
