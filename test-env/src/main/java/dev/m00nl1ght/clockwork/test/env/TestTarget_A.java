package dev.m00nl1ght.clockwork.test.env;

import dev.m00nl1ght.clockwork.container.ImmutableComponentContainer;
import dev.m00nl1ght.clockwork.core.ComponentContainer;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;

public class TestTarget_A implements ComponentTarget {

    private final TestTarget_C testTargetC;
    private final ImmutableComponentContainer componentContainer;

    public TestTarget_A(TargetType<?> targetType, TestTarget_C testTargetC) {
        this.testTargetC = testTargetC;
        componentContainer = new ImmutableComponentContainer(targetType, this);
        componentContainer.initComponents();
    }

    public TestTarget_C getTestTargetC() {
        return testTargetC;
    }

    @Override
    public ComponentContainer getComponentContainer() {
        return componentContainer;
    }

}
