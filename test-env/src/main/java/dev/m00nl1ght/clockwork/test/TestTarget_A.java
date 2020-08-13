package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ComponentContainer;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;

public class TestTarget_A implements ComponentTarget {

    public static final TargetType<TestTarget_A> TARGET_TYPE = TestLauncher.getTargetType(TestTarget_A.class);

    protected final ComponentContainer<?> container;

    protected TestTarget_A() {
        this.container = buildContainer();
    }

    protected ComponentContainer<?> buildContainer() {
        return new ComponentContainer<>(TARGET_TYPE, this);
    }

    @Override
    public ComponentContainer<?> getComponentContainer() {
        return container;
    }

}
