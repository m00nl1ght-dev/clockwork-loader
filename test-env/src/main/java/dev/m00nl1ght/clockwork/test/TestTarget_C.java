package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ComponentContainer;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;

public class TestTarget_C implements ComponentTarget {

    private final ComponentContainer<TestTarget_C> container;

    protected TestTarget_C(TargetType<TestTarget_C> targetType) {
        this.container = new ComponentContainer<>(targetType, this);
        this.container.initComponents();
    }

    @Override
    public TargetType<TestTarget_C> getTargetType() {
        return container.getTargetType();
    }

    @Override
    public Object getComponent(int internalID) {
        return container.getComponent(internalID);
    }

}
