package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ComponentContainer;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.ProfilingComponentContainer;
import dev.m00nl1ght.clockwork.debug.profiler.core.CoreProfiler;

public class TestTarget_C implements ComponentTarget {

    public static final TargetType<TestTarget_C> TARGET_TYPE = TestLauncher.getTargetType(TestTarget_C.class);

    private final ComponentContainer<TestTarget_C> container;

    protected TestTarget_C(CoreProfiler profiler) {
        this.container = new ProfilingComponentContainer<>(TARGET_TYPE, this, profiler);
    }

    @Override
    public ComponentContainer<?> getComponentContainer() {
        return container;
    }

}
