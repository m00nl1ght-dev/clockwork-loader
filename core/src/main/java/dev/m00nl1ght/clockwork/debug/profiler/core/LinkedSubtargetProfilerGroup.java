package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.FunctionalSubtarget;
import dev.m00nl1ght.clockwork.core.TargetType;

public class LinkedSubtargetProfilerGroup<T extends ComponentTarget, F> extends SubtargetProfilerGroup<T, F> {

    private final SubtargetProfilerGroup<? super T, F> parent;

    public LinkedSubtargetProfilerGroup(String name, TargetType<T> targetType, FunctionalSubtarget<T, F> subtarget, SubtargetProfilerGroup<? super T, F> parent) {
        super(name, targetType);
        this.parent = parent;
        final var components = subtarget.getComponents();
        this.compEntries = new SubtargetProfilerEntry[components.size()];
        final var splitIdx = parent.compEntries.length;

        for (int i = 0; i < splitIdx; i++) {
            compEntries[i] = new LinkedSubtargetProfilerEntry<>(subtarget, components.get(i), parent.get(i));
        }

        for (int i = splitIdx; i < compEntries.length; i++) {
            compEntries[i] = new SubtargetProfilerEntry<>(subtarget, components.get(i));
        }
    }

}
