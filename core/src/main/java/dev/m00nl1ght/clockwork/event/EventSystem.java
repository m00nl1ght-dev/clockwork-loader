package dev.m00nl1ght.clockwork.event;

import dev.m00nl1ght.clockwork.core.PluginLoadingException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class EventSystem<T> {

    private boolean baked = false;
    private Map<Class<?>, List<BiConsumer<?, ?>>> listeners;
    private Map<Class<?>, EventType<?, T>> types;

    public void registerEvent(Class<?> eventClass) {
        if (baked) throw new IllegalStateException("EventSystem is already baked");
        final var prev = listeners.putIfAbsent(eventClass, new LinkedList<>());
        if (prev != null) throw PluginLoadingException.generic("Event class [" + eventClass.getSimpleName() + "] already registered");
    }

    public <E, C> void registerListener(Class<E> eventClass, Class<C> componentClass, BiConsumer<C, E> listener) {
        if (baked) throw new IllegalStateException("EventSystem is already baked");
        final var list = listeners.get(eventClass);
        if (list == null) throw PluginLoadingException.generic("Event class [" + eventClass.getSimpleName() + "] is not registered");
        list.add(listener);
    }

    public <E> EventType<E, T> getEventType(Class<E> eventClass) {
        if (!baked) throw new IllegalStateException("EventSystem is not baked yet");
        final var type = types.get(eventClass);
        if (type == null) throw new IllegalArgumentException("Event type for class [" + eventClass.getSimpleName() + "] not found");
        return (EventType<E, T>) type;
    }

    public void bake() {
        if (baked) throw new IllegalStateException("EventSystem is already baked");
        baked = true;
        listeners.forEach(this::bakeType);
    }

    private <E> void bakeType(Class<E> eventClass, List<BiConsumer<?, ?>> listeners) {
        types.put(eventClass, new EventType<>(this, eventClass, listeners));
    }

}
