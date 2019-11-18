package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.event.Event;

import java.util.HashMap;
import java.util.Map;

public class EventDispatcherRegistry {

    private final Map<Class<?>, EventDispatcherFactory<?>> registry = new HashMap<>();

    public EventDispatcherRegistry() {
        register(new DefaultFactory());
    }

    public void register(EventDispatcherFactory<?> factory) {
        registry.put(factory.getTarget(), factory);
    }

    @SuppressWarnings("unchecked")
    private <E extends U, U, T extends ComponentTarget> EventDispatcher<E, T> getExact(Class<E> eventClass, Class<U> factoryClass, ComponentTargetType<T> target) {
        final var factory = (EventDispatcherFactory<U>) registry.get(factoryClass);
        return factory == null ? null : factory.build(target, eventClass);
    }

    public <E, T extends ComponentTarget> EventDispatcher<E, T> getDispatcherFor(Class<E> eventClass, ComponentTargetType<T> target) {
        var sc = eventClass.getSuperclass();
        while (sc != null) {
            final var dispatcher = getExact(eventClass, sc, target);
            if (dispatcher != null) return dispatcher;
            sc = sc.getSuperclass();
        }

        throw PluginLoadingException.noEventDispatcherFactoryFound(eventClass);
    }

    private static class DefaultFactory implements EventDispatcherFactory<Event> {

        @Override
        public <T extends ComponentTarget, E extends Event> EventDispatcher<E, T> build(ComponentTargetType<T> targetType, Class<E> eventClass) {
            return new EventDispatcher<>(targetType, eventClass);
        }

        @Override
        public Class<Event> getTarget() {
            return Event.class;
        }

    }

}
