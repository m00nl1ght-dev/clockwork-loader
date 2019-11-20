package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.*;

public class TestTarget_A implements ComponentTarget<TestTarget_A> {

    private final ComplexComponentContainer<?, TestTarget_A> container;

    protected TestTarget_A(TargetType<? extends TestTarget_A> targetType) {
        this.container = new ComplexComponentContainer<>(targetType, this);
        this.container.initComponents();
    }

    @Override
    public TargetType<? extends TestTarget_A> getTargetType() {
        return container.getTargetType();
    }

    @Override
    public <C> C getComponent(ComponentType<C, ? extends TestTarget_A> componentType) {
        return container.getComponent(componentType);
    }

}
