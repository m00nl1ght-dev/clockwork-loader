package dev.m00nl1ght.clockwork.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

    public static boolean tryFindSupertype(Type type, Type supertype) {
        if (type.equals(supertype)) return true;
        if (type instanceof Class) {
            final var classType = (Class<?>) type;
            final var sc = classType.getGenericSuperclass();
            if (sc != null && tryFindSupertype(sc, supertype)) return true;
            return Arrays.stream(classType.getGenericInterfaces())
                    .anyMatch(i -> tryFindSupertype(i, supertype));
        }
        return false;
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
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + className + " with params " + params);
        }
    }

}
