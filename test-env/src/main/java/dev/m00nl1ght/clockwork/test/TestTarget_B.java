package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ComponentContainer;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.ProfilingComponentContainer;
import dev.m00nl1ght.clockwork.debug.profiler.core.CoreProfiler;

public class TestTarget_B extends TestTarget_A {

    public static final TargetType<TestTarget_B> TARGET_TYPE = TestLauncher.getTargetType(TestTarget_B.class);

    protected TestTarget_B(CoreProfiler profiler) {
        super(profiler);
    }

    @Override
    protected ComponentContainer<?> buildContainer(CoreProfiler profiler) {
        return new ProfilingComponentContainer<>(TARGET_TYPE, this, profiler);
    }

}
