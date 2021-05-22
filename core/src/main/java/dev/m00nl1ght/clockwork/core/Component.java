package dev.m00nl1ght.clockwork.core;

import java.util.Objects;

public abstract class Component<T extends ComponentTarget> {

    protected final T target;

    protected Component(T target) {
        this.target = Objects.requireNonNull(target);
    }

    public T getTarget() {
        return target;
    }

}
