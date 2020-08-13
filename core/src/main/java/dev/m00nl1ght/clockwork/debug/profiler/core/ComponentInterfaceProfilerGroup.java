package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.interfaces.ComponentInterfaceType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.ProfilerEntry;
import dev.m00nl1ght.clockwork.debug.profiler.ProfilerGroup;

import java.util.List;

public class ComponentInterfaceProfilerGroup<I, T extends ComponentTarget> extends ProfilerGroup {

    protected final TargetType<T> targetType;
    protected ComponentInterfaceProfilerEntry[] compEntries;

    protected ComponentInterfaceProfilerGroup(String name, TargetType<T> targetType) {
        super(name);
        this.targetType = targetType;
    }

    public ComponentInterfaceProfilerGroup(String name, TargetType<T> targetType, ComponentInterfaceType<I, T> interfaceType) {
        this(name, targetType);
        final var components = interfaceType.getComponents(targetType);
        this.compEntries = new ComponentInterfaceProfilerEntry[components.size()];
        for (int i = 0; i < components.size(); i++) {
            compEntries[i] = new ComponentInterfaceProfilerEntry<>(interfaceType, components.get(i));
        }
    }

    @Override
    public List<ProfilerEntry> getEntries() {
        return List.of(compEntries);
    }

    @SuppressWarnings("unchecked")
    public ComponentInterfaceProfilerEntry<I, T> get(int idx) {
        return compEntries[idx];
    }

}
