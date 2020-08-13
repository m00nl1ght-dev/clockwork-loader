package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.interfaces.ComponentInterfaceType;
import dev.m00nl1ght.clockwork.debug.profiler.SimpleCyclicProfilerEntry;

public class ComponentInterfaceProfilerEntry<I, T extends ComponentTarget> extends SimpleCyclicProfilerEntry {

    public static final int CAPACITY = 100;

    protected final ComponentType<?, ? super T> componentType;

    public ComponentInterfaceProfilerEntry(ComponentInterfaceType<I, T> interfaceType, ComponentType<?, ? super T> componentType) {
        super(componentType.getId(), CAPACITY);
        this.componentType = componentType;
    }

}
