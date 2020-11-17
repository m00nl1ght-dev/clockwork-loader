package dev.m00nl1ght.clockwork.events.listener;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.util.TypeRef;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.BiConsumer;

public abstract class EventListener<E extends Event, T extends ComponentTarget, C> {

    public static final Comparator<EventListener<?, ?, ?>> PRIORITY_ORDER =
            Comparator.comparingInt(o -> o.priority.ordinal());

    protected final ComponentType<C, T> componentType;
    protected final EventListenerPriority priority;
    protected final TypeRef<E> eventType;

    protected EventListener(@NotNull TypeRef<E> eventType,
                            @NotNull ComponentType<C, T> componentType,
                            @NotNull EventListenerPriority priority) {

        this.componentType = Objects.requireNonNull(componentType);
        this.priority = Objects.requireNonNull(priority);
        this.eventType = Objects.requireNonNull(eventType);
    }

    public @NotNull TypeRef<E> getEventType() {
        return eventType;
    }

    public @NotNull ComponentType<C, T> getComponentType() {
        return componentType;
    }

    public @NotNull EventListenerPriority getPriority() {
        return priority;
    }

    public abstract @NotNull BiConsumer<C, E> getConsumer();

    @Override
    public String toString() {
        return eventType + "@" + componentType.toString() + "[" + priority + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventListener)) return false;
        EventListener<?, ?, ?> that = (EventListener<?, ?, ?>) o;
        return componentType.equals(that.componentType) &&
                eventType.equals(that.eventType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentType, eventType);
    }

}
