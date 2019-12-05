package dev.m00nl1ght.clockwork.event.listener;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.debug.ProfilerEntry;

public interface EventListener<E, C, T extends ComponentTarget> {

    void accept(T object, C component, E event);

    void accept(T object, C component, E event, ProfilerEntry profilerEntry);

    ComponentType<C, T> getComponentType();

}
