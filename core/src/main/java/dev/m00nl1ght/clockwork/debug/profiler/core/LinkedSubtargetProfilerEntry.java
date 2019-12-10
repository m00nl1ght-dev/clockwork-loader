package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.FunctionalSubtarget;

public class LinkedSubtargetProfilerEntry<T extends ComponentTarget, F> extends SubtargetProfilerEntry<T, F> {

    private final SubtargetProfilerEntry<? super T, F> parent;

    public LinkedSubtargetProfilerEntry(FunctionalSubtarget<T, F> subtarget, ComponentType<?, ? super T> componentType, SubtargetProfilerEntry<? super T, F> parent) {
        super(subtarget, componentType);
        this.parent = parent;
    }

    @Override
    public void put(int value) {
        super.put(value);
        parent.put(value);
    }

}
