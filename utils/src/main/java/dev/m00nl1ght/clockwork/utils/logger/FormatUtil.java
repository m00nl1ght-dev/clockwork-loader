package dev.m00nl1ght.clockwork.utils.logger;

public class FormatUtil {

    private FormatUtil() {}

    public static String format(String str, Object... objects) {
        var p = 0;
        for (var object : objects) {
            if (p >= str.length()) break;
            p = Math.min(str.indexOf("[]", p), str.indexOf("{}", p));
            if (p < 0) break;
            final var os = object.toString();
            final var keepBr = str.charAt(p) == '[';
            str = str.substring(0, keepBr ? p + 1 : p) + os + str.substring(keepBr ? p + 1 : p + 2);
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

    public static IllegalAccessException illAccExc(String msg, Object... objects) {
        return new IllegalAccessException(format(msg, objects));
    }

    public static IllegalStateException illStateExc(String msg, Object... objects) {
        return new IllegalStateException(format(msg, objects));
    }

    public static UnsupportedOperationException unspExc(String msg, Object... objects) {
        return new UnsupportedOperationException(format(msg, objects));
    }

}
