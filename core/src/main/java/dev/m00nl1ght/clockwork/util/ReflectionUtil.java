package dev.m00nl1ght.clockwork.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Type;
import java.util.Arrays;

public class ReflectionUtil {

    public static MethodHandle tryFindConstructor(MethodHandles.Lookup lookup, Class<?> targetClass, Class<?>... params) {
        try {
            Arguments.notNullAnd(lookup, MethodHandles.Lookup::hasFullPrivilegeAccess, "lookup");
            ReflectionUtil.class.getModule().addReads(Arguments.notNull(targetClass, "targetClass").getModule());
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

}
