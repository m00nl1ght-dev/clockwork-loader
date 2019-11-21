package dev.m00nl1ght.clockwork.core;

import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class FunctionalSubtarget<T extends ComponentTarget<? super T>, F> {

    private final TargetType<T> target;
    private final Class<F> type;
    private final int[] compIdxs;

    protected FunctionalSubtarget(TargetType<T> target, Class<F> type) {
        this.target = target;
        this.type = checkType(type);
        if (!target.isInitialised()) throw new IllegalStateException("cannot create subtarget before target is primed");
        final var list = target.getRegisteredTypes().stream()
                .filter(c -> type.isAssignableFrom(c.getComponentClass()))
                .collect(Collectors.toList());
        this.compIdxs = new int[list.size()];
        for (var i = 0; i < compIdxs.length; i++) {
            compIdxs[i] = list.get(i).getInternalID();
        }
    }

    public void apply(ComponentContainer<? super T> container, Consumer<F> consumer) {
        for (var idx : compIdxs) {
            final var comp = container.components[idx];
            if (comp != null) consumer.accept((F) comp);
        }
    }

    private Class<F> checkType(Class<F> type) {
        if (type.isPrimitive() || type.isArray() || type.isEnum() || type.isSynthetic()) {
            throw new IllegalArgumentException("Invalid type for FunctionalSubtarget: " + type.getName());
        } else {
            return type;
        }
    }

}