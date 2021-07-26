package dev.m00nl1ght.clockwork.component;

import dev.m00nl1ght.clockwork.util.ReflectionUtil;

import java.lang.invoke.MethodHandles;

public interface ComponentFactory<T extends ComponentTarget, C> {

    ComponentFactory EMPTY = t -> null;

    C create(T obj) throws Throwable;

    @SuppressWarnings("unchecked")
    static <T extends ComponentTarget, C> ComponentFactory<T, C> emptyFactory() {
        return EMPTY;
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
