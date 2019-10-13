package dev.m00nl1ght.clockwork.util;

import java.lang.reflect.Constructor;

public class ReflectionUtil {

    public static <T> Constructor<T> getConstructorOrNull(Class<T> clazz, Class<?>... params) {
        try {
            return clazz.getConstructor(params);
        } catch (Exception e) {
            return null;
        }
    }

}
