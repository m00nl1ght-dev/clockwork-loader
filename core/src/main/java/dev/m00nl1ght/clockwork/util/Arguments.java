package dev.m00nl1ght.clockwork.util;

import java.util.List;
import java.util.function.Predicate;

public class Arguments {

    public static <T> T nullOr(T object, Predicate<T> test, String name) {
        if (object != null && !test.test(object))
            throw FormatUtil.illArgExc("Argument [] is invalid", name);
        return object;
    }

    public static <T> T notNull(T object, String name) {
        if (object == null)
            throw FormatUtil.illArgExc("Argument [] must not be null", name);
        return object;
    }

    public static String notNullOrEmpty(String str, String name) {
        if (str == null)
            throw FormatUtil.illArgExc("Argument [] must not be null", name);
        if (str.isEmpty())
            throw FormatUtil.illArgExc("Argument [] must not be empty", name);
        return str;
    }

    public static String notNullOrBlank(String str, String name) {
        if (str == null)
            throw FormatUtil.illArgExc("Argument [] must not be null", name);
        if (str.isBlank())
            throw FormatUtil.illArgExc("Argument [] must not be blank", name);
        return str;
    }

    public static <T> T notNullAnd(T object, Predicate<T> test, String name) {
        if (object == null)
            throw FormatUtil.illArgExc("Argument [] must not be null", name);
        if (!test.test(object))
            throw FormatUtil.illArgExc("Argument [] is invalid", name);
        return object;
    }

    public static <T> Class<T> verifyType(Class<T> type, Class<?> target, String name) {
        if (!target.isAssignableFrom(type))
            throw FormatUtil.illArgExc("Argument [] must be an instance of []", name, target.getSimpleName());
        return type;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> listSnapshot(List<? extends T> list, String name) {
        if (list == null)
            throw FormatUtil.illArgExc("Argument [] list must not be null");
        return (List<T>) List.of(list.toArray());
    }

    public static <T> List<T> verifiedList(List<T> list, Predicate<T> test, String name) {
        for (final var e : list) if (!test.test(e))
            throw FormatUtil.illArgExc("Argument [] list contains invalid element []", name, e);
        return list;
    }

    public static <T> List<T> verifiedListSnapshot(List<? extends T> list, Predicate<T> test, String name) {
        return verifiedList(listSnapshot(list, name), test, name);
    }

}
