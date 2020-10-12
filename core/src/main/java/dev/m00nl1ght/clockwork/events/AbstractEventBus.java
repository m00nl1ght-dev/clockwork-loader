package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.events.listener.EventListenerPriority;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Set;
import java.util.function.BiConsumer;

public abstract class AbstractEventBus implements EventBus {

    protected final ClockworkCore core;

    protected AbstractEventBus(ClockworkCore core) {
        this.core = core;
    }

    @Override
    public <E extends Event, T extends ComponentTarget> EventDispatcher<E, T> getEventDispatcher(TypeRef<E> eventType, Class<T> targetClass) {
        return null; // TODO
    }

    @Override
    public final <E extends Event, T extends ComponentTarget, C>
    EventListener<E, T, C> addListener(TypeRef<E> eventType, Class<T> targetClass, Class<C> componentClass, BiConsumer<C, E> consumer, EventListenerPriority priority) {
        final var component = core.getComponentType(componentClass, targetClass);
        if (component.isEmpty()) throw FormatUtil.illArgExc("No component type for class [] found", componentClass);
        return this.addListener(component.get(), consumer, priority);
    }

    @Override
    public void post(Event event) {
        // TODO
    }

    @Override
    public Set<EventDispatcher<?, ?>> getEventDispatchers() {
        return null; // TODO
    }

    public final <E extends Event, T extends ComponentTarget, C>
    EventListener<E, T, C> addListener(ComponentType<C, T> componentType, BiConsumer<C, E> consumer) {
        return this.addListener(componentType, consumer, EventListenerPriority.NORMAL);
    }

    public final <E extends Event, T extends ComponentTarget, C>
    EventListener<E, T, C> addListener(ComponentType<C, T> componentType, BiConsumer<C, E> consumer, EventListenerPriority priority) {
        return null; // TODO
    }

}
