package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.*;

public class TestComponentTarget implements ComponentTarget {

    private final ComponentContainer<? extends TestComponentTarget> container;

    public <U extends TestComponentTarget> TestComponentTarget(ComponentTargetType<U> targetType) {
        this.container = new ComponentContainer<>(targetType, (U) this);
        this.container.initComponents();
    }

    @Override
    public <C> C getComponent(ComponentType<C, ?> componentType) {
        return container.getComponent(componentType);
    }

}
