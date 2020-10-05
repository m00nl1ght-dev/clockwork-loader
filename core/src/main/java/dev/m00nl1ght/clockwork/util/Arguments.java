package dev.m00nl1ght.clockwork.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
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

    public static <T> List<T> snapshot(Collection<? extends T> list, String name) {
        if (list == null)
            throw FormatUtil.illArgExc("Argument [] list must not be null");
        return (List<T>) List.of(list.toArray());
    }

    public static <K, V> Map<K, V> snapshot(Map<? extends K, ? extends V> map, String name) {
        if (map == null)
            throw FormatUtil.illArgExc("Argument [] map must not be null");
        return Map.copyOf(map);
    }

    public static <T> List<T> notNull(List<T> list, Predicate<T> test, String name) {
        for (final var e : Arguments.notNull(list, name)) if (e == null)
            throw FormatUtil.illArgExc("Argument [] list contains null element", name);
        return list;
    }

    public static <T> List<T> verifiedList(List<T> list, Predicate<T> test, String name) {
        for (final var e : Arguments.notNull(list, name)) if (!test.test(e))
            throw FormatUtil.illArgExc("Argument [] list contains invalid element []", name, e);
        return list;
    }

    public static <T> List<T> verifiedSnapshot(Collection<? extends T> list, Predicate<T> test, String name) {
        return verifiedList(snapshot(list, name), test, name);
    }

    public static <T> T[] asArray(Collection<? extends T> collection, String name) {
        return (T[]) Arguments.notNull(collection, name).toArray();
    }

    public static <T> T[] asNotNullArray(Collection<? extends T> collection, String name) {
        for (final var e : Arguments.notNull(collection, name)) if (e == null)
            throw FormatUtil.illArgExc("Argument [] collection contains null element", name);
        return (T[]) collection.toArray();
    }

    public static <T> T[] asVerifiedArray(Collection<? extends T> collection, Predicate<T> test, String name) {
        for (final var e : Arguments.notNull(collection, name)) if (!test.test(e))
            throw FormatUtil.illArgExc("Argument [] collection contains invalid element []", name, e);
        return (T[]) collection.toArray();
    }

    public static <T, K> void distinct(Collection<T> collection, Function<? super T, ? extends K> function, String name) {
        final var keys = new HashSet<K>();
        for (final var e : collection) {
            final var key = function.apply(e);
            if (!keys.add(key))
                throw FormatUtil.illArgExc("Argument [] collection contains indistinct element []", name, e);
        }
    }

    public static int inRange(int num, int min, int max, String name) {
        if (num < min || num > max)
            throw FormatUtil.illArgExc("Argument [] is not in valid range", name);
        return num;
    }

}
