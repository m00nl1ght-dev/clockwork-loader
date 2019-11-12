package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.*;

public class TestComponentTarget implements ComponentTarget<TestComponentTarget> {

    private final ComponentContainer<? extends TestComponentTarget> container;

    public <T extends TestComponentTarget> TestComponentTarget(ComponentTargetType<T> targetType) {
        this.container = new ComponentContainer<>(targetType, (T) this);
        this.container.initComponents();
    }

    @Override
    public <C> C getComponent(ComponentType<C, ? extends TestComponentTarget> componentType) {
        return container.getComponent(componentType);
    }

}
