package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.container.ImmutableComponentContainer;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;

public class TestTarget_C implements ComponentTarget {

    public static final TargetType<TestTarget_C> TARGET_TYPE = TestLauncher.getTargetType(TestTarget_C.class);

    private final ImmutableComponentContainer<TestTarget_C> container;

    protected TestTarget_C() {
        this.container = new ImmutableComponentContainer<>(TARGET_TYPE, this);
        this.container.initComponents();
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
