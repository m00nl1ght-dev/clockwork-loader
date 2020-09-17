package dev.m00nl1ght.clockwork.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ReflectionUtil {

    private static final Logger LOGGER = LogManager.getLogger();

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

}
