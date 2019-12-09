package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.FunctionalSubtarget;
import dev.m00nl1ght.clockwork.debug.profiler.ProfilerGroup;

public class SubtargetProfilerGroup<T extends ComponentTarget, F> extends ProfilerGroup {

    private final FunctionalSubtarget<T, F> subtarget;

    public SubtargetProfilerGroup(FunctionalSubtarget<T, F> subtarget) {
        super(subtarget.getType().getSimpleName());
        this.subtarget = subtarget;
    }

}
