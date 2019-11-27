package dev.m00nl1ght.clockwork.core;

import java.util.function.Consumer;
import java.util.stream.Collectors;

// TODO rework to a similiar system like events, respecting components of subclasses

public class FunctionalSubtarget<T extends ComponentTarget, F> {

    private final TargetType<T> target;
    private final Class<F> type;
    private final int[] compIdxs;
    private final ComponentType[] comps;

    protected FunctionalSubtarget(TargetType<T> target, Class<F> type) {
        this.target = target;
        this.type = checkType(type);
        if (!target.isInitialised()) throw new IllegalStateException("cannot create subtarget before target is initialised");
        final var list = target.getRegisteredTypes().stream()
                .filter(c -> type.isAssignableFrom(c.getComponentClass()))
                .collect(Collectors.toList());
        this.compIdxs = new int[list.size()];
        this.comps = new ComponentType[list.size()];
        for (var i = 0; i < compIdxs.length; i++) {
            final var comp = list.get(i);
            comps[i] = comp;
            compIdxs[i] = comp.getInternalID();
        }
    }

    @SuppressWarnings("unchecked")
    public void apply(T object, Consumer<F> consumer) {
        var idx = -1;
        try {
            for (idx = 0; idx < comps.length; idx++) {
                final var comp = object.getComponent(idx);
                if (comp != null) consumer.accept((F) comp);
            }
        } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
            if (target.canAcceptFrom(object.getTargetType())) {
                throw e;
            } else {
                throw new IllegalArgumentException("FunctionalSubtarget of target [" + target + "] cannot be applied for target [" + object.getTargetType() + "]");
            }
        } catch (Throwable throwable) {
            throw ExceptionInPlugin.inFunctionalSubtarget(this, comps[idx], throwable);
        }
    }

    private Class<F> checkType(Class<F> type) {
        if (type.isPrimitive() || type.isArray() || type.isEnum() || type.isSynthetic()) {
            throw new IllegalArgumentException("Invalid type for FunctionalSubtarget: " + type.getName());
        } else {
            return type;
        }
    }

    public Class<F> getType() {
        return type;
    }

}
