package dev.m00nl1ght.clockwork.descriptor;

import java.util.regex.Pattern;

public class Namespaces {

    private Namespaces() {}

    public static final Pattern SIMPLE_ID_PATTERN = Pattern.compile("^[a-z][.a-z0-9_-]{2,30}[a-z0-9]$");
    public static final Pattern COMBINED_ID_PATTERN = Pattern.compile("^([a-z][.a-z0-9_-]{2,30}[a-z0-9])(?::([a-z][.a-z0-9_-]{2,30}[a-z0-9]))?$");
    public static final Pattern DEPENDENCY_PATTERN = Pattern.compile("^([a-z][.a-z0-9_-]{2,30}[a-z0-9])(?::([a-z][.a-z0-9_-]{2,30}[a-z0-9]))?(?:\\[(.*)])?$");

    public static String simpleId(String input) {
        final var matcher = SIMPLE_ID_PATTERN.matcher(input);
        if (matcher.matches()) {
            return input;
        } else {
            throw new RuntimeException("Invalid id: " + input);
        }
    }

    public static String combinedId(String input) {
        final var matcher = COMBINED_ID_PATTERN.matcher(input);
        if (matcher.matches()) {
            return input;
        } else {
            throw new RuntimeException("Invalid combined id: " + input);
        }
    }

    public static String combinedId(String input, String autoFirst) {
        final var result = combinedIdOrNull(input, autoFirst);
        if (result == null) throw new RuntimeException("Invalid id: " + input);
        return result;
    }

    public static String combinedIdOrNull(String input, String autoFirst) {
        final var matcher = COMBINED_ID_PATTERN.matcher(input);
        if (matcher.matches()) {
            final var second = matcher.group(2);
            if (second != null || input.equals(autoFirst)) return autoFirst;
            return combine(autoFirst, input);
        } else {
            return null;
        }
    }

    public static String combinedIdWithFirst(String input, String first) {
        final var matcher = COMBINED_ID_PATTERN.matcher(input);
        if (matcher.matches()) {
            final var second = matcher.group(2);
            if (second == null) {
                if (input.equals(first)) return first;
                return combine(first, input);
            } else if (matcher.group(1).equals(first)) {
                return input;
            } else {
                throw new RuntimeException("Expected combined id starting with '" + first + "' but found: " + input);
            }
        } else {
            throw new RuntimeException("Invalid combined id: " + input);
        }
    }

    public static String combine(String first, String second) {
        return second == null ? first : first + ":" + second;
    }

    public static String first(String input) {
        final var i = input.indexOf(':');
        return i < 0 ? input : input.substring(0, i);
    }

    public static String second(String input) {
        final var i = input.indexOf(':');
        return i < 0 ? null : input.substring(i + 1);
    }

}
