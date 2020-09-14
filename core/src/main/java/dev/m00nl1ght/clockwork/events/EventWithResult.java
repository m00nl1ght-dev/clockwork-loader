package dev.m00nl1ght.clockwork.events;

import java.util.Objects;

public abstract class EventWithResult<R> extends Event {

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

}
