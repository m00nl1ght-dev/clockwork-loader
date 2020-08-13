package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.interfaces.ComponentInterfaceType;

public class LinkedComponentInterfaceProfilerEntry<I, T extends ComponentTarget> extends ComponentInterfaceProfilerEntry<I, T> {

    private final ComponentInterfaceProfilerEntry<I, ? super T> parent;

    public LinkedComponentInterfaceProfilerEntry(ComponentInterfaceType<I, T> interfaceType, ComponentType<?, ? super T> componentType, ComponentInterfaceProfilerEntry<I, ? super T> parent) {
        super(interfaceType, componentType);
        this.parent = parent;
    }

    @Override
    public void put(int value) {
        super.put(value);
        parent.put(value);
    }

}
