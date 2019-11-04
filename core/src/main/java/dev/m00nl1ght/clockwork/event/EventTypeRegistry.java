package dev.m00nl1ght.clockwork.event;

import dev.m00nl1ght.clockwork.core.ComponentTargetType;
import dev.m00nl1ght.clockwork.core.PluginLoadingException;

import java.util.HashMap;
import java.util.Map;

public class EventTypeRegistry {

    private final Map<Class<?>, EventTypeFactory<?>> registry = new HashMap<>();

    public EventTypeRegistry() {
        register(new DefaultFactory());
    }

    public void register(EventTypeFactory<?> factory) {
        registry.put(factory.getTarget(), factory);
    }

    @SuppressWarnings("unchecked")
    private <E extends U, U, T> EventType<E, T> getExact(Class<E> eventClass, Class<U> factoryClass, ComponentTargetType<T> target) {
        final var factory = (EventTypeFactory<U>) registry.get(factoryClass);
        return factory == null ? null : factory.build(target, eventClass);
    }

    public <E, T> EventType<E, T> getEventTypeFor(Class<E> eventClass, ComponentTargetType<T> target) {
        var sc = eventClass.getSuperclass();
        while (sc != null) {
            final var type = getExact(eventClass, sc, target);
            if (type != null) return type;
            sc = sc.getSuperclass();
        }

        throw PluginLoadingException.noEventTypeFactoryFound(eventClass);
    }

    private static class DefaultFactory implements EventTypeFactory<Event> {

        @Override
        public <T, E extends Event> EventType<E, T> build(ComponentTargetType<T> targetType, Class<E> eventClass) {
            return new EventType<>(targetType, eventClass);
        }

        @Override
        public Class<Event> getTarget() {
            return Event.class;
        }

    }

}
