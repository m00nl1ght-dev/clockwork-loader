package dev.m00nl1ght.clockwork.event.impl.listener;

import dev.m00nl1ght.clockwork.component.ComponentTarget;
import dev.m00nl1ght.clockwork.component.ComponentType;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.event.EventListener;
import dev.m00nl1ght.clockwork.utils.reflect.TypeRef;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiConsumer;

public class EventListenerSimple<E extends Event, T extends ComponentTarget, C> extends EventListener<E, T, C> {

    private final BiConsumer<C, E> consumer;

    public EventListenerSimple(TypeRef<E> eventType, ComponentType<C, T> componentType, Phase phase, BiConsumer<C, E> consumer) {
        super(eventType, componentType, phase);
        this.consumer = Objects.requireNonNull(consumer);
    }

    public EventListenerSimple(Class<E> eventClass, ComponentType<C, T> componentType, Phase phase, BiConsumer<C, E> consumer) {
        this(TypeRef.of(eventClass), componentType, phase, consumer);
    }

    @Override
    public BiConsumer<C, E> getConsumer() {
        return consumer;
    }

    @Override
    public @NotNull String getUniqueID() {
        return consumer.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventListenerSimple)) return false;
        if (!super.equals(o)) return false;
        EventListenerSimple<?, ?, ?> that = (EventListenerSimple<?, ?, ?>) o;
        return consumer.equals(that.consumer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), consumer);
    }

}
