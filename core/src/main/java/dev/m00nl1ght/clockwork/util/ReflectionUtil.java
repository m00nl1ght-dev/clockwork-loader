package dev.m00nl1ght.clockwork.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class ReflectionUtil {

    public static MethodHandle tryFindConstructor(MethodHandles.Lookup lookup, Class<?> targetClass, Class<?>... params) {
        try {
            Objects.requireNonNull(lookup);
            Objects.requireNonNull(targetClass);
            if (!lookup.hasFullPrivilegeAccess()) throw new IllegalArgumentException();
            ReflectionUtil.class.getModule().addReads(targetClass.getModule());
            final var privateLookup = MethodHandles.privateLookupIn(targetClass, lookup);
            return privateLookup.findConstructor(targetClass, MethodType.methodType(void.class, params));
        } catch (NoSuchMethodException e) {
            return null;
        } catch (Throwable t) {
            throw FormatUtil.rtExc(t, "Failed to extract constructor from []", targetClass.getSimpleName());
        }
    }

    public static <T> T buildInstance(Class<T> baseClass, String className, List<String> params) {
        try {
            final var clazz = Class.forName(className);
            final var desc = new Class[params.size()];
            Arrays.fill(desc, String.class);
            final var constr = clazz.getConstructor(desc);
            @SuppressWarnings("unchecked")
            final T instance = (T) constr.newInstance(params.toArray());
            return instance;
        } catch (Throwable t) {
            throw FormatUtil.rtExc(t, "Failed to create instance of [] with params []", className, params);
        }
    }

    public static boolean parameterizedTypeEquals(Type type, Type otherRaw, Type[] otherTypeParams) {
        if (type instanceof ParameterizedType) {
            final var paType = (ParameterizedType) type;
            final var paRaw = paType.getRawType();
            final var paParams = paType.getActualTypeArguments();
            return Objects.equals(paRaw, otherRaw) && Arrays.equals(paParams, otherTypeParams);
        } else {
            return false;
        }
    }

    public static boolean tryFindSupertype(Class<?> type, Class<?> supertype, Type typeParam) {
        return tryFindSupertype(type, supertype, new Type[]{typeParam});
    }

    public static boolean tryFindSupertype(Class<?> type, Class<?> supertype, Type[] typeParams) {
        return tryFindSupertype(type, st -> parameterizedTypeEquals(st, supertype, typeParams));
    }

    public static boolean tryFindSupertype(Class<?> type, Type supertype) {
        return tryFindSupertype(type, st -> st.equals(supertype));
    }

    public static boolean tryFindSupertype(Class<?> type, Predicate<Type> predicate) {
        final var queue = new ArrayDeque<Type>();
        queue.add(type);

        while (!queue.isEmpty()) {
            final var next = queue.poll();
            if (predicate.test(next)) {
                return true;
            } else if (next instanceof Class) {
                final var cl = (Class<?>) next;
                queue.addAll(Arrays.asList(cl.getGenericInterfaces()));
                final var sc = cl.getGenericSuperclass();
                if (sc != null) queue.add(sc);
            } else if (next instanceof ParameterizedType) {
                final var pt = (ParameterizedType) next;
                queue.add(pt.getRawType());
            }
        }

        return false;
    }

}
