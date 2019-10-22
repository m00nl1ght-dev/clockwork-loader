package dev.m00nl1ght.clockwork.event;

import dev.m00nl1ght.clockwork.core.ComponentTarget;

import java.util.ArrayList;
import java.util.List;

public class EventType<E, T extends ComponentTarget<T>> {

    private final List<EventListener<?, E, T>> listeners = new ArrayList<>();
    private final EventSystem<T> system;

    protected EventType(EventSystem<T> system) {
        this.system = system;
    }

    public void post(E event, T object) {
        for (var eventListener : listeners) {
            eventListener.accept(event, object);
        }
    }

    protected void registerListener(EventListener<?, E, T> listener) {
        listeners.add(listener);
    }

    public EventSystem<T> getSystem() {
        return system;
    }

}
