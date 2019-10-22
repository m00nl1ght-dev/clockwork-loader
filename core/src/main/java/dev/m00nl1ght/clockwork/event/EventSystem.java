package dev.m00nl1ght.clockwork.event;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentTargetType;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.PluginLoadingException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@SuppressWarnings("unchecked")
public class EventSystem<T extends ComponentTarget<T>> {

    private final Map<Class<?>, EventType<?, T>> types = new HashMap<>();
    private final ComponentTargetType<T> targetType;

    public EventSystem(ComponentTargetType<T> targetType) {
        this.targetType = targetType;
    }

    public void registerEvent(Class<?> eventClass) {
        final var prev = types.putIfAbsent(eventClass, new EventType<>(this));
        if (prev != null) throw PluginLoadingException.generic("Event class [" + eventClass.getSimpleName() + "] already registered");
    }

    public <E, C> void registerListener(Class<E> eventClass, ComponentType<C, T> component, BiConsumer<C, E> listener) {
        final var type = getEventType(eventClass);
        type.registerListener(new EventListener<>(type, component, listener));
    }

    public <E> EventType<E, T> getEventType(Class<E> eventClass) {
        final var type = types.get(eventClass);
        if (type == null) throw new IllegalArgumentException("Event type for class [" + eventClass.getSimpleName() + "] not found");
        return (EventType<E, T>) type;
    }

    public ComponentTargetType<T> getTargetType() {
        return targetType;
    }

}
