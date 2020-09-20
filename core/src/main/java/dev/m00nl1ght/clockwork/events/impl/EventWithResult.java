package dev.m00nl1ght.clockwork.events.impl;

import java.util.Objects;

public abstract class EventWithResult<R> extends ContextAwareEvent {

    protected R result;
    protected final R initialResult;

    protected EventWithResult(R initialResult) {
        this.initialResult = Objects.requireNonNull(initialResult);
    }

    public R getInitialResult() {
        return initialResult;
    }

    public R getResult() {
        return result;
    }

    public void setResult(R result) {
        checkModificationAllowed();
        this.result = Objects.requireNonNull(result);
    }

    @Override
    public String toString() {
        return super.toString() + "{result=" + result + "}";
    }

}
