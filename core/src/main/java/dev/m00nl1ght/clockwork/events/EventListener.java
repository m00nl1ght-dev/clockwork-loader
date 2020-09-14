package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Comparator;
import java.util.function.BiConsumer;

public final class EventListener<E extends Event, T extends ComponentTarget, C> {

    public static final Comparator<EventListener<?, ?, ?>> PRIORITY_ORDER = Comparator.comparingInt(o -> o.priority.ordinal());

    private final ComponentType<C, T> componentType;
    private final EventListenerPriority priority;
    private final TypeRef<E> eventClassType;
    private final BiConsumer<C, E> consumer;

    public EventListener(TypeRef<E> eventClassType, ComponentType<C, T> componentType, EventListenerPriority priority, BiConsumer<C, E> consumer) {
        this.componentType = componentType;
        this.priority = priority;
        this.eventClassType = eventClassType;
        this.consumer = consumer;
    }

    public EventListener(Class<E> eventClass, ComponentType<C, T> componentType, EventListenerPriority priority, BiConsumer<C, E> consumer) {
        this(TypeRef.of(eventClass), componentType, priority, consumer);
    }

    public TypeRef<E> getEventClassType() {
        return eventClassType;
    }

    public ComponentType<C, T> getComponentType() {
        return componentType;
    }

    public EventListenerPriority getPriority() {
        return priority;
    }

    public BiConsumer<C, E> getConsumer() {
        return consumer;
    }

    @Override
    public String toString() {
        return eventClassType + "@" + componentType + "[" + priority + "]";
    }

}
