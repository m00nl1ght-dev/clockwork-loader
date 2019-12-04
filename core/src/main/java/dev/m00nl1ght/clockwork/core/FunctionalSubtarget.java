package dev.m00nl1ght.clockwork.core;

import java.util.function.Consumer;

public class FunctionalSubtarget<T extends ComponentTarget, F> {

    private final TargetType<?> rootTarget;
    private final Class<F> type;
    private final int internalId;

    FunctionalSubtarget(Class<F> type, TargetType<?> rootTarget, int internalId) {
        this.rootTarget = rootTarget;
        this.type = type;
        this.internalId = internalId;
    }

    public void apply(T object, Consumer<F> consumer) {
        try {
            object.getTargetType().applySubtarget(internalId, object, type, consumer);
        } catch (Exception e) {
            this.rootTarget.checkCompatibility(object.getTargetType());
            throw e;
        }
    }

    public int getInternalId() {
        return internalId;
    }

    public TargetType<?> getRootTarget() {
        return rootTarget;
    }

    public Class<F> getType() {
        return type;
    }

    static class Empty<T extends ComponentTarget, F> extends FunctionalSubtarget<T, F> {

        Empty(Class<F> type, TargetType<?> rootTarget) {
            super(type, rootTarget, -1);
        }

        @Override
        public void apply(T object, Consumer<F> consumer) {}

    }

}
