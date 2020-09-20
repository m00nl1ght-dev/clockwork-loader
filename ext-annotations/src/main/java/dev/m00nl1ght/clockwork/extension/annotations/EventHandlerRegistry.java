package dev.m00nl1ght.clockwork.extension.annotations;

import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.*;
import java.util.stream.Collectors;

public class EventHandlerRegistry {

    private final Map<TypeRef<?>, Set<EventHandlerMethod<?, ?>>> byEventType;
    private final Map<Class<?>, Set<EventHandlerMethod<?, ?>>> byHandlerClass;

    private EventHandlerRegistry(Builder builder) {
        this.byHandlerClass = Map.copyOf(builder.handlers);
        this.byEventType = byHandlerClass.values().stream().flatMap(Collection::stream)
                .collect(Collectors.groupingBy(EventHandlerMethod::getEventClassType, Collectors.toUnmodifiableSet()));
    }

    @SuppressWarnings("unchecked")
    public <E extends Event> Set<EventHandlerMethod<E, ?>> getForEventType(TypeRef<E> eventClassType) {
        return (Set<EventHandlerMethod<E, ?>>) (Set<?>) byEventType.get(eventClassType);
    }

    @SuppressWarnings("unchecked")
    public <C> Set<EventHandlerMethod<?, C>> getForHandlerClass(Class<C> handlerClass) {
        return (Set<EventHandlerMethod<?, C>>) (Set<?>) byHandlerClass.get(handlerClass);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Map<Class<?>, Set<EventHandlerMethod<?, ?>>> handlers = new HashMap<>();

        private Builder() {}

        public EventHandlerRegistry build() {
            return new EventHandlerRegistry(this);
        }

        public <C> void add(EventHandlerMethod<?, C> handlerMethod) {
            Arguments.notNull(handlerMethod, "handlerMethod");
            this.handlers.computeIfAbsent(handlerMethod.getComponentClass(), c -> new LinkedHashSet<>()).add(handlerMethod);
        }

        public <C> void put(Class<C> handlerClass, Collection<EventHandlerMethod<?, C>> handlers) {
            Arguments.notNull(handlerClass, "handlerClass");
            Arguments.notNull(handlers, "handlers");
            this.handlers.put(handlerClass, Set.copyOf(handlers));
        }

    }

}
