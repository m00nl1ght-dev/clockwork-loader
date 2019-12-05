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

    @SuppressWarnings("unchecked")
    public void apply(T object, Consumer<F> consumer) {
        final var container = (ComponentContainer<T>) object.getComponentContainer();
        try {
            container.applySubtarget(this, consumer);
        } catch (Exception e) {
            container.getTargetType().checkCompatibilityForSubtarget(rootTarget);
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
