package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.FunctionalSubtarget;
import dev.m00nl1ght.clockwork.debug.profiler.SimpleCyclicProfilerEntry;

public class SubtargetProfilerEntry<T extends ComponentTarget, F> extends SimpleCyclicProfilerEntry {

    public static final int CAPACITY = 100;

    protected final ComponentType<?, ? super T> componentType;

    public SubtargetProfilerEntry(FunctionalSubtarget<T, F> subtarget, ComponentType<?, ? super T> componentType) {
        super(componentType.getId(), CAPACITY);
        this.componentType = componentType;
    }

}
