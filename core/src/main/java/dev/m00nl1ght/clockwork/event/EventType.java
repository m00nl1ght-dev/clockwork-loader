package dev.m00nl1ght.clockwork.event;

import dev.m00nl1ght.clockwork.core.ComponentTarget;

import java.util.List;

@SuppressWarnings("unchecked")
public class EventType<E, T extends ComponentTarget<T>> {

    private final EventListener[] listeners;
    private final EventSystem<T> system;

    protected EventType(EventSystem<T> system, List<EventListener<?, E, T>> listeners) {
        this.listeners = listeners.toArray(EventListener[]::new);
        this.system = system;
    }

    public void post(E event, T object) {
        for (var eventListener : listeners) {
            ((EventListener<?, E, T>) eventListener).accept(event, object);
        }
    }

    public EventSystem<T> getSystem() {
        return system;
    }

}
