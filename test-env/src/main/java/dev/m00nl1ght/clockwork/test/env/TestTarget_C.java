package dev.m00nl1ght.clockwork.test.env;

import dev.m00nl1ght.clockwork.container.ImmutableComponentContainer;
import dev.m00nl1ght.clockwork.core.ComponentContainer;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;

public class TestTarget_C implements ComponentTarget {

    private final ImmutableComponentContainer componentContainer;

    public TestTarget_C(TargetType<?> targetType) {
        componentContainer = new ImmutableComponentContainer(targetType, this);
        componentContainer.initComponents();
    }

    @Override
    public ComponentContainer getComponentContainer() {
        return componentContainer;
    }

}
