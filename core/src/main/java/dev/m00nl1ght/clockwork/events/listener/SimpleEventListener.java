package dev.m00nl1ght.clockwork.events.listener;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Objects;
import java.util.function.BiConsumer;

public class SimpleEventListener<E extends Event, T extends ComponentTarget, C> extends EventListener<E, T, C> {

    private final BiConsumer<C, E> consumer;

    public SimpleEventListener(TypeRef<E> eventType, ComponentType<C, T> componentType, EventListenerPriority priority, BiConsumer<C, E> consumer) {
        super(eventType, componentType, priority);
        this.consumer = Objects.requireNonNull(consumer);
    }

    public SimpleEventListener(Class<E> eventClass, ComponentType<C, T> componentType, EventListenerPriority priority, BiConsumer<C, E> consumer) {
        this(TypeRef.of(eventClass), componentType, priority, consumer);
    }

    @Override
    public BiConsumer<C, E> getConsumer() {
        return consumer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleEventListener)) return false;
        if (!super.equals(o)) return false;
        SimpleEventListener<?, ?, ?> that = (SimpleEventListener<?, ?, ?>) o;
        return consumer.equals(that.consumer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), consumer);
    }

}
