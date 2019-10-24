package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ComponentContainer;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentTargetType;
import dev.m00nl1ght.clockwork.core.ComponentType;

public class TestComponentTarget implements ComponentTarget<TestComponentTarget> {

    private final ComponentContainer<TestComponentTarget> container;

    public TestComponentTarget(ComponentTargetType<TestComponentTarget> testTargetType) {
        this.container = new ComponentContainer<>(testTargetType, this);
        this.container.initComponents();
    }

    @Override
    public <C> C getComponent(ComponentType<C, TestComponentTarget> componentType) {
        return container.getComponent(componentType);
    }

}
