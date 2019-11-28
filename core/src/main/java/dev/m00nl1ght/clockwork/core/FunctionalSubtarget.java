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

    @SuppressWarnings("unchecked")
    public void apply(T object, Consumer<F> consumer) {
        /* TODO
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
        */
    }

    public TargetType<T> getTarget() {
        return target;
    }

    public Class<F> getType() {
        return type;
    }

}
