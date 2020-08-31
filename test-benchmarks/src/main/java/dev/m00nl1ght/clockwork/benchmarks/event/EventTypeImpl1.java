package dev.m00nl1ght.clockwork.benchmarks.event;

import dev.m00nl1ght.clockwork.benchmarks.TestEvent;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.EventListener;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Arrays;

public class EventTypeImpl1<E extends TestEvent, T extends ComponentTarget> extends TestEventType<E, T> {

    private static final EventListener[] EMPTY_ARRAY = new EventListener[0];

    private EventListener[][] compiledListeners;

    public EventTypeImpl1(TypeRef<E> eventClassType, Class<T> targetClass) {
        super(eventClassType, targetClass);
    }

    public EventTypeImpl1(Class<E> eventClass, Class<T> targetClass) {
        super(eventClass, targetClass);
    }

    public EventTypeImpl1(TypeRef<E> eventClassType, TargetType<T> targetType) {
        super(eventClassType, targetType);
    }

    public EventTypeImpl1(Class<E> eventClass, TargetType<T> targetType) {
        super(eventClass, targetType);
    }

    @Override
    protected void init() {
        super.init();
        final var cnt = getTargetType().getSubtargetIdxLast() - idxOffset + 1;
        this.compiledListeners = new EventListener[cnt][];
        Arrays.fill(compiledListeners, EMPTY_ARRAY);
    }

    @Override
    protected void onListenersChanged(TargetType<? extends T> targetType) {
        final var listeners = getEffectiveListeners(targetType);
        final var idx = targetType.getSubtargetIdxFirst() - idxOffset;
        this.compiledListeners[idx] = listeners.toArray(EventListener[]::new);
    }

    @Override
    @SuppressWarnings("unchecked")
    public E post(T object, E event) {
        final var container = object.getComponentContainer();
        final var target = container.getTargetType();
        if (target.getRoot() != rootTarget) checkCompatibility(target);
        try {
            final var listeners = compiledListeners[target.getSubtargetIdxFirst() - idxOffset];
            for (int i = 0; i < listeners.length; i++) {
                event.listener = listeners[i];
                final var listener = listeners[i];
                final var component = container.getComponent(listener.getComponentIdx());
                if (component != null) listener.getConsumer().accept(component, event);
            }
            return event;
        } catch (Throwable t) {
            checkCompatibility(target);
            throw t;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public E postContextless(T object, E event) {
        final var container = object.getComponentContainer();
        final var target = container.getTargetType();
        if (target.getRoot() != rootTarget) checkCompatibility(target);
        try {
            final var listeners = compiledListeners[target.getSubtargetIdxFirst() - idxOffset];
            for (int i = 0; i < listeners.length; i++) {
                final var listener = listeners[i];
                final var component = container.getComponent(listener.getComponentIdx());
                if (component != null) listener.getConsumer().accept(component, event);
            }
            return event;
        } catch (Throwable t) {
            checkCompatibility(target);
            throw t;
        }
    }

}
