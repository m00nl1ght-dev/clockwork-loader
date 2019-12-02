package dev.m00nl1ght.clockwork.core;

import java.util.function.Consumer;

// TODO rework to a similiar system like events, respecting components of subclasses

public class FunctionalSubtarget<T extends ComponentTarget, F> {

    private final TargetType<T> target;
    private final Class<F> type;
    private final int internalId;

    FunctionalSubtarget(Class<F> type, TargetType<T> target, int internalId) {
        this.target = target;
        this.type = type;
        this.internalId = internalId;
    }

    public void apply(T object, Consumer<F> consumer) {
        try {
            object.getTargetType().applySubtarget(internalId, object, type, consumer);
        } catch (Exception e) {
            this.target.checkCompatibility(object);
            throw e;
        }
    }

    public TargetType<T> getTarget() {
        return target;
    }

    public Class<F> getType() {
        return type;
    }

}
