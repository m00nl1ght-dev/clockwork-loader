package dev.m00nl1ght.clockwork.events;

import java.util.Objects;

public abstract class EventWithResult<R> implements Event {

    private R result;
    private final R initialResult;

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
        // TODO check event dispatch stage
        this.result = Objects.requireNonNull(result);
    }

}
