package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ComponentContainer;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.ProfilingComponentContainer;
import dev.m00nl1ght.clockwork.debug.profiler.core.CoreProfiler;

public class TestTarget_A implements ComponentTarget {

    public static final TargetType<TestTarget_A> TARGET_TYPE = TestLauncher.getTargetType(TestTarget_A.class);

    protected final ComponentContainer<?> container;

    protected TestTarget_A(CoreProfiler profiler) {
        this.container = buildContainer(profiler);
    }

    protected ComponentContainer<?> buildContainer(CoreProfiler profiler) {
        return new ProfilingComponentContainer<>(TARGET_TYPE, this, profiler);
    }

    @Override
    public ComponentContainer<?> getComponentContainer() {
        return container;
    }

}
