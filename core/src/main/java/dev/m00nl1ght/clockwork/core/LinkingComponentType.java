package dev.m00nl1ght.clockwork.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LinkingComponentType<C extends ComponentTarget, T extends ComponentTarget> extends ComponentType<C, T> {

    protected final TargetType<C> innerTargetType;

    public LinkingComponentType(
            @Nullable LinkingComponentType<? super C, ? super T> parent,
            @NotNull TargetType<C> innerTargetType,
            @NotNull TargetType<T> outerTargetType) {

        super(parent, innerTargetType.getTargetClass(), outerTargetType);
        this.innerTargetType = innerTargetType;
    }

    public final @NotNull TargetType<C> getInnerTargetType() {
        return innerTargetType;
    }

    @Override
    public void checkValue(T target, C value) {
        if (value != null && value.getTargetType().getRoot() != innerTargetType.getRoot())
            throw new RuntimeException("Invalid value: " + value);
    }

}
