package dev.m00nl1ght.clockwork.events.listener;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventListenerPriority;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.function.BiConsumer;

public class SimpleEventListener<E extends Event, T extends ComponentTarget, C> extends EventListener<E, T, C> {

    private final BiConsumer<C, E> consumer;

    public SimpleEventListener(TypeRef<E> eventClassType, ComponentType<C, T> componentType, EventListenerPriority priority, BiConsumer<C, E> consumer) {
        super(eventClassType, componentType, priority);
        this.consumer = Arguments.notNull(consumer, "consumer");
    }

    public SimpleEventListener(Class<E> eventClass, ComponentType<C, T> componentType, EventListenerPriority priority, BiConsumer<C, E> consumer) {
        this(TypeRef.of(eventClass), componentType, priority, consumer);
    }

    @Override
    public BiConsumer<C, E> getConsumer() {
        return consumer;
    }

}
