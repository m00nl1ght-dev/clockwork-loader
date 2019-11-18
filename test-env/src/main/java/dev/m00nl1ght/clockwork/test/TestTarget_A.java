package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.*;

public class TestTarget_A implements ComponentTarget {

    private final ComponentContainer<? extends TestTarget_A> container;

    protected TestTarget_A(ComponentTargetType<? extends TestTarget_A> targetType) {
        this.container = new ComponentContainer<>(targetType, this);
        this.container.initComponents();
    }

    @Override
    public ComponentTargetType<?> getTargetType() {
        return container.getTargetType();
    }

    @Override
    public <C> C getComponent(ComponentType<C, ?> componentType) {
        return container.getComponent(componentType);
    }

}
