package dev.m00nl1ght.clockwork.events.listener;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventListenerPriority;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Comparator;
import java.util.function.BiConsumer;

public abstract class EventListener<E extends Event, T extends ComponentTarget, C> {

    public static final Comparator<EventListener<?, ?, ?>> PRIORITY_ORDER = Comparator.comparingInt(o -> o.priority.ordinal());

    protected final ComponentType<C, T> componentType;
    protected final EventListenerPriority priority;
    protected final TypeRef<E> eventClassType;

    protected EventListener(TypeRef<E> eventClassType, ComponentType<C, T> componentType, EventListenerPriority priority) {
        this.componentType = Arguments.notNull(componentType, "componentType");
        this.priority = Arguments.notNull(priority, "priority");
        this.eventClassType = Arguments.notNull(eventClassType, "eventClassType");
        componentType.getTargetType().requireInitialised();
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

    public abstract BiConsumer<C, E> getConsumer();

    @Override
    public String toString() {
        return eventClassType + "@" + componentType.toString() + "[" + priority + "]";
    }

}
