package dev.m00nl1ght.clockwork.utils.logger;

public final class FormatUtil {

    private FormatUtil() {}

    public static String format(String str, Object... objects) {
        final var sb = new StringBuilder(str);

        var p = 0;
        var keepBr = false;

        for (var object : objects) {

            final var max = sb.length() - 1;
            for (int i = p; i < max; i++) {
                if (sb.charAt(i) == '[' && sb.charAt(i + 1) == ']') {
                    p = i; keepBr = true; break;
                } else if (sb.charAt(i) == '{' && sb.charAt(i+ 1) == '}') {
                    p = i; keepBr = false; break;
                }
            }

            if (p >= max) break;

            final var os = object.toString();
            if (keepBr) {
                sb.insert(p + 1, os);
            } else {
                sb.replace(p, p + 2, os);
            }

            p += os.length() + (keepBr ? 2 : 0);
        }

        return sb.toString();
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
