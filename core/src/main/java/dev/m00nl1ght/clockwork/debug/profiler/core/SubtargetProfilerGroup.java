package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.FunctionalSubtarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.ProfilerEntry;
import dev.m00nl1ght.clockwork.debug.profiler.ProfilerGroup;

import java.util.List;

public class SubtargetProfilerGroup<T extends ComponentTarget, F> extends ProfilerGroup {

    protected final TargetType<T> targetType;
    protected SubtargetProfilerEntry[] compEntries;

    protected SubtargetProfilerGroup(String name, TargetType<T> targetType) {
        super(name);
        this.targetType = targetType;
    }

    public SubtargetProfilerGroup(String name, TargetType<T> targetType, FunctionalSubtarget<T, F> subtarget) {
        this(name, targetType);
        final var components = subtarget.getComponents();
        this.compEntries = new SubtargetProfilerEntry[components.size()];
        for (int i = 0; i < components.size(); i++) {
            compEntries[i] = new SubtargetProfilerEntry<>(subtarget, components.get(i));
        }
    }

    @Override
    public List<ProfilerEntry> getEntries() {
        return List.of(compEntries);
    }

    @SuppressWarnings("unchecked")
    public SubtargetProfilerEntry<T, F> get(int idx) {
        return compEntries[idx];
    }

}
