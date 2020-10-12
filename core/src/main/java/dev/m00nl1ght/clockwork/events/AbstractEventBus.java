package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.events.listener.EventListenerPriority;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public abstract class AbstractEventBus<B extends Event> implements EventBus<B> {

    protected final ClockworkCore core;
    protected final Map<TypeRef<?>, Map<Class<?>, EventDispatcher<? extends B, ?>>> dispatchers = new LinkedHashMap<>();

    protected AbstractEventBus(ClockworkCore core) {
        this.core = Arguments.notNull(core, "core");
        core.getState().requireOrAfter(ClockworkCore.State.POPULATED);
    }

    @Override
    public <E extends B, T extends ComponentTarget>
    EventDispatcher<E, T> getEventDispatcher(TypeRef<E> eventType, Class<T> targetClass) {
        final var eventMap = dispatchers.computeIfAbsent(eventType, e -> new LinkedHashMap<>());
        @SuppressWarnings("unchecked")
        final var existing = (EventDispatcher<E, T>) eventMap.get(targetClass);
        if (existing != null) {
            return existing;
        } else {
            final var dispatcher = buildDispatcher(eventType, targetClass);
            eventMap.put(targetClass, dispatcher);
            return dispatcher;
        }
    }

    protected abstract <E extends B, T extends ComponentTarget>
    EventDispatcher<E, T> buildDispatcher(TypeRef<E> eventType, Class<T> targetClass);

    @Override
    public final <E extends B, T extends ComponentTarget, C>
    EventListener<E, T, C> addListener(TypeRef<E> eventType, Class<T> targetClass, Class<C> componentClass, BiConsumer<C, E> consumer, EventListenerPriority priority) {
        final var component = core.getComponentType(componentClass, targetClass);
        if (component.isEmpty()) throw FormatUtil.illArgExc("No component type for class [] found", componentClass);
        return this.addListener(eventType, component.get(), consumer, priority);
    }

    @Override
    public Set<EventDispatcher<? extends B, ?>> getEventDispatchers() {
        return dispatchers.values().stream().flatMap(e -> e.values().stream()).collect(Collectors.toUnmodifiableSet());
    }

    public final <E extends B, T extends ComponentTarget, C>
    EventListener<E, T, C> addListener(TypeRef<E> eventType, ComponentType<C, T> componentType, BiConsumer<C, E> consumer) {
        return this.addListener(eventType, componentType, consumer, EventListenerPriority.NORMAL);
    }

    public final <E extends B, T extends ComponentTarget, C>
    EventListener<E, T, C> addListener(TypeRef<E> eventType, ComponentType<C, T> componentType, BiConsumer<C, E> consumer, EventListenerPriority priority) {
        return getEventDispatcher(eventType, componentType.getTargetType().getTargetClass()).addListener(componentType, consumer, priority);
    }

    public ClockworkCore getCore() {
        return core;
    }

}
