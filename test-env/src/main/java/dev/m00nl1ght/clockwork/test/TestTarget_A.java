package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.*;

public class TestTarget_A implements ComponentTarget {

    private final ComponentContainer<? extends TestTarget_A> container;

    @SuppressWarnings("unchecked")
    protected <T extends TestTarget_A> TestTarget_A(TargetType<T> targetType) {
        this.container = new ComponentContainer<>(targetType, (T) this);
        this.container.initComponents();
    }

    @Override
    public TargetType<? extends TestTarget_A> getTargetType() {
        return container.getTargetType();
    }

    @Override
    public Object getComponent(int internalID) {
        return container.getComponent(internalID);
    }

}
