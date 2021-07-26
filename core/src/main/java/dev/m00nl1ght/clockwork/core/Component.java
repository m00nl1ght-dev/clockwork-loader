package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.component.ComponentTarget;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class Component<T extends ComponentTarget> {

    protected final T target;

    protected Component(@NotNull T target) {
        this.target = Objects.requireNonNull(target);
    }

    public T getTarget() {
        return target;
    }

}
