package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;

import java.util.function.BiConsumer;

public final class EventListener<E extends Event, T extends ComponentTarget, C> {

    private final ComponentType<C, T> componentType;
    private final EventListenerPriority priority;
    private final Class<E> eventClass;
    private final BiConsumer<C, E> consumer;

    public EventListener(Class<E> eventClass, ComponentType<C, T> componentType, EventListenerPriority priority, BiConsumer<C, E> consumer) {
        this.componentType = componentType;
        this.priority = priority;
        this.eventClass = eventClass;
        this.consumer = consumer;
    }

    public Class<E> getEventClass() {
        return eventClass;
    }

    public ComponentType<C, T> getComponentType() {
        return componentType;
    }

    public EventListenerPriority getPriority() {
        return priority;
    }

}
