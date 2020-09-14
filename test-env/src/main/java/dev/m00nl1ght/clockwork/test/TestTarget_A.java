package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.container.ImmutableComponentContainer;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;

public class TestTarget_A implements ComponentTarget {

    public static final TargetType<TestTarget_A> TARGET_TYPE = TestLauncher.getTargetType(TestTarget_A.class);

    protected final ImmutableComponentContainer<?> container;

    protected TestTarget_A() {
        this.container = buildContainer();
        this.container.initComponents();
    }

    protected ImmutableComponentContainer<?> buildContainer() {
        return new ImmutableComponentContainer<>(TARGET_TYPE, this);
    }

    @Override
    public Object getComponent(int internalID) {
        return container.getComponent(internalID);
    }

    @Override
    public TargetType<?> getTargetType() {
        return container.getTargetType();
    }

}
