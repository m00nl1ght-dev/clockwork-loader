package dev.m00nl1ght.clockwork.test.env;

import dev.m00nl1ght.clockwork.component.impl.SimpleComponentContainer;
import dev.m00nl1ght.clockwork.component.ComponentContainer;
import dev.m00nl1ght.clockwork.component.ComponentTarget;
import dev.m00nl1ght.clockwork.component.TargetType;

public class TestTarget_C implements ComponentTarget {

    private final SimpleComponentContainer componentContainer;

    public TestTarget_C(TargetType<?> targetType) {
        componentContainer = new SimpleComponentContainer(targetType, this);
        componentContainer.initComponents();
    }

    @Override
    public ComponentContainer getComponentContainer() {
        return componentContainer;
    }

}
