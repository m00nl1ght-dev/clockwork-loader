package dev.m00nl1ght.clockwork.event;

import java.util.List;
import java.util.function.BiConsumer;

public class EventType<E, T> {

    private final BiConsumer[] listeners;
    private final EventSystem<T> system;

    protected EventType(EventSystem<T> system, Class<E> eventClass, List<BiConsumer<?, ?>> listeners) {
        this.listeners = listeners.toArray(BiConsumer[]::new);
        this.system = system;
    }

    public void post(E event, T object) {

    }

}
