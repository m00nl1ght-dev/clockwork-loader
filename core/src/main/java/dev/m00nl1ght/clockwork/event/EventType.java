package dev.m00nl1ght.clockwork.event;

import dev.m00nl1ght.clockwork.core.ComponentTargetType;
import dev.m00nl1ght.clockwork.core.ComponentType;

import java.util.function.BiConsumer;

public class EventType<E, T> {

    private EventListener<?, E, T> listenerChainFirst;
    private EventListener<?, E, T> listenerChainLast;
    private final ComponentTargetType<T> target;

    public EventType(ComponentTargetType<T> target) {
        this.target = target;
    }

    public void post(E event, T object) {
        var c = listenerChainFirst;
        while (c != null) {
            c.accept(event, object);
            c = c.next;
        }
    }

    public final synchronized <C> void registerListener(ComponentType<C, T> componentType, BiConsumer<C, E> consumer) {
        final var last = new EventListener<>(this, componentType, consumer);
        listenerChainLast.next = last;
        listenerChainLast = last;
    }

    public final ComponentTargetType<T> getTarget() {
        return target;
    }

}
