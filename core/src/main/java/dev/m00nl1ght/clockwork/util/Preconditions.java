package dev.m00nl1ght.clockwork.util;

import java.util.function.Predicate;

public class Preconditions {

    public static <T> T notNull(T object, String name) {
        if (object == null) throw new IllegalArgumentException(name + " must not be null");
        return object;
    }

    public static String notNullOrEmpty(String str, String name) {
        if (str == null) throw new IllegalArgumentException(name + " must not be null");
        if (str.isEmpty()) throw new IllegalArgumentException(name + " must not be empty");
        return str;
    }

    public static String notNullOrBlank(String str, String name) {
        if (str == null) throw new IllegalArgumentException(name + " must not be null");
        if (str.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return str;
    }

    public static <T> T notNullAnd(T object, Predicate<T> test, String name) {
        if (object == null) throw new IllegalArgumentException(name + " must not be null");
        if (!test.test(object)) throw new IllegalArgumentException(name + " is invalid");
        return object;
    }

    public static <T> Class<T> verifyType(Class<T> type, Class<?> target, String name) {
        if (!target.isAssignableFrom(type)) throw new IllegalArgumentException(name + " must be an instance of " + target.getSimpleName());
        return type;
    }

}
