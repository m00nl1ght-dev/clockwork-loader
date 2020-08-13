package dev.m00nl1ght.clockwork.benchmarks.event;

import dev.m00nl1ght.clockwork.benchmarks.event.dispatcher.EmptyEventDispatcher;
import dev.m00nl1ght.clockwork.benchmarks.event.dispatcher.EventDispatcher;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.ExceptionInPlugin;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class SimpleEventType<E extends Event, T extends ComponentTarget> extends EventType<E, T> {

    private EventDispatcher[] dispatchers;
    private TargetType<? super T> rootTarget;
    private int idxOffset;

    public SimpleEventType(Class<E> eventClass, Class<T> targetClass) {
        super(eventClass, targetClass);
    }

    @Override
    protected void init() {
        this.rootTarget = getTargetType().getRoot();
        this.idxOffset = getTargetType().getSubtargetIdxFirst();
        final var cnt = getTargetType().getSubtargetIdxLast() - idxOffset;
        this.dispatchers = new EventDispatcher[cnt];
        Arrays.fill(dispatchers, EmptyEventDispatcher.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    public E post(T object, E event) {
        final var target = object.getComponentContainer().getTargetType();
        if (target.getRoot() != rootTarget) target.checkCompatibility(this);
        final var dispatcher = dispatchers[target.getSubtargetIdxFirst() - idxOffset];
        try {
            dispatcher.post(object, event);
            return event;
        } catch (ExceptionInPlugin e) {
            throw e;
        } catch (Throwable t) {
            target.checkCompatibility(this);
            throw ExceptionInPlugin.inEventHandler(null, event, object, t); // TODO
        }
    }

    @SuppressWarnings("unchecked")
    public <S extends T> List<ComponentType<?, ? super S>> getListeners(TargetType<S> target) {
        if (target.getRoot() != rootTarget) target.checkCompatibility(this);
        try {
            final var dispatcher = dispatchers[target.getSubtargetIdxFirst() - idxOffset];
            return dispatcher.getListeners();
        } catch (Exception e) {
            target.checkCompatibility(this);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public <C> void addListener(ComponentType<C, ? extends T> componentType, BiConsumer<C, E> listener) {
        final var target = Objects.requireNonNull(componentType).getTargetType();
        if (target.getRoot() != rootTarget) target.checkCompatibility(this);
        try {
            final var dispatcher = dispatchers[target.getSubtargetIdxFirst() - idxOffset];
            if (dispatcher == EmptyEventDispatcher.INSTANCE) {
                // TODO
            } else {
                dispatcher.addListener(componentType, listener);
            }
        } catch (Exception e) {
            target.checkCompatibility(this);
            throw e;
        }
    }

}
