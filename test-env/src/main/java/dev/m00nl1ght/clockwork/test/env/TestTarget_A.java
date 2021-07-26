package dev.m00nl1ght.clockwork.test.env;

import dev.m00nl1ght.clockwork.component.impl.SimpleComponentContainer;
import dev.m00nl1ght.clockwork.component.ComponentContainer;
import dev.m00nl1ght.clockwork.component.ComponentTarget;
import dev.m00nl1ght.clockwork.component.TargetType;

public class TestTarget_A implements ComponentTarget {

    private final TestTarget_C testTargetC;
    private final SimpleComponentContainer componentContainer;

    public TestTarget_A(TargetType<?> targetType, TestTarget_C testTargetC) {
        this.testTargetC = testTargetC;
        componentContainer = new SimpleComponentContainer(targetType, this);
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
