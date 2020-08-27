package dev.m00nl1ght.clockwork.benchmarks;

import dev.m00nl1ght.clockwork.core.ComponentContainer;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;

public class TestTarget implements ComponentTarget {

    public static final TargetType<TestTarget> TARGET_TYPE =
            ClockworkBenchmarks.clockworkCore.getTargetType(TestTarget.class).orElseThrow();

    private final ComponentContainer<TestTarget> container;

    public TestTarget() {
        this.container = new ComponentContainer<>(TARGET_TYPE, this);
        this.container.initComponents();
    }

    @Override
    public ComponentContainer<?> getComponentContainer() {
        return container;
    }

}
