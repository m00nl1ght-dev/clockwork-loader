package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.ReflectionUtil;

import java.lang.invoke.MethodHandles;

public interface ComponentFactory<T extends ComponentTarget, C> {

    C create(T obj) throws Throwable;

    static <T extends ComponentTarget, C> ComponentFactory<T, C> emptyFactory() {
        return t -> null;
    }

    @SuppressWarnings("unchecked")
    static <T extends ComponentTarget, C> ComponentFactory<T, C>
    buildDefaultFactory(MethodHandles.Lookup lookup, Class<T> targetClass, Class<C> componentClass) {
        final var objCtr = ReflectionUtil.tryFindConstructor(lookup, componentClass, targetClass);
        if (objCtr != null) return o -> (C) objCtr.invoke(o);
        final var emptyCtr = ReflectionUtil.tryFindConstructor(lookup, componentClass);
        if (emptyCtr != null) return o -> (C) emptyCtr.invoke();
        return null;
    }

}
