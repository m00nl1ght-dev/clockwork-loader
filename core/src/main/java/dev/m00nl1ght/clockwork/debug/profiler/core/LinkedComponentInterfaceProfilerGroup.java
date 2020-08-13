package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.interfaces.ComponentInterfaceType;
import dev.m00nl1ght.clockwork.core.TargetType;

public class LinkedComponentInterfaceProfilerGroup<I, T extends ComponentTarget> extends ComponentInterfaceProfilerGroup<I, T> {

    private final ComponentInterfaceProfilerGroup<I, ? super T> parent;

    public LinkedComponentInterfaceProfilerGroup(String name, TargetType<T> targetType, ComponentInterfaceType<I, T> interfaceType, ComponentInterfaceProfilerGroup<I, ? super T> parent) {
        super(name, targetType);
        this.parent = parent;
        final var components = interfaceType.getComponents(targetType);
        this.compEntries = new ComponentInterfaceProfilerEntry[components.size()];
        final var splitIdx = parent.compEntries.length;

        for (int i = 0; i < splitIdx; i++) {
            compEntries[i] = new LinkedComponentInterfaceProfilerEntry<>(interfaceType, components.get(i), parent.get(i));
        }

        for (int i = splitIdx; i < compEntries.length; i++) {
            compEntries[i] = new ComponentInterfaceProfilerEntry<>(interfaceType, components.get(i));
        }
    }

}
