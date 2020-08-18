package dev.m00nl1ght.clockwork.benchmarks.event;

import dev.m00nl1ght.clockwork.benchmarks.TestEvent;
import dev.m00nl1ght.clockwork.core.ComponentContainer;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.BasicEventType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventListener;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class EventTypeImpl0<E extends TestEvent, T extends ComponentTarget> extends BasicEventType<E, T> {

    private EventDispatcher[] dispatchers;

    public EventTypeImpl0(TypeRef<E> eventClassType, Class<T> targetClass) {
        super(eventClassType, targetClass);
    }

    public EventTypeImpl0(Class<E> eventClass, Class<T> targetClass) {
        super(eventClass, targetClass);
    }

    public EventTypeImpl0(TypeRef<E> eventClassType, TargetType<T> targetType) {
        super(eventClassType, targetType);
    }

    public EventTypeImpl0(Class<E> eventClass, TargetType<T> targetType) {
        super(eventClass, targetType);
    }

    @Override
    protected void init() {
        final var cnt = getTargetType().getSubtargetIdxLast() - idxOffset;
        this.dispatchers = new EventDispatcher[cnt];
        Arrays.fill(dispatchers, EmptyEventDispatcher.INSTANCE);
    }

    @Override
    protected void onListenersChanged(TargetType<? extends T> targetType) {
        final var listeners = getEffectiveListeners(targetType);
        final var idx = targetType.getSubtargetIdxFirst() - idxOffset;
        if (listeners.isEmpty()) {
            dispatchers[idx] = EmptyEventDispatcher.INSTANCE;
        } else if (listeners.size() == 1) {
            dispatchers[idx] = new SingleEventDispatcher(listeners.get(0));
        } else {
            dispatchers[idx] = new MultiEventDispatcher(listeners);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public E post(T object, E event) {
        final var target = object.getComponentContainer().getTargetType();
        if (target.getRoot() != rootTarget) target.checkCompatibility(this);
        final var dispatcher = dispatchers[target.getSubtargetIdxFirst() - idxOffset];
        event.dispatcher = dispatcher;
        try {
            dispatcher.dispatch(object.getComponentContainer(), event);
            return event;
        } catch (Throwable t) {
            target.checkCompatibility(this);
            throw t;
        }
    }

    public interface EventDispatcher<T extends ComponentTarget, E extends Event> {

        void dispatch(ComponentContainer<?> container, E event);

        List<EventListener<E, ? extends T, ?>> getListeners();

    }

    private static class EmptyEventDispatcher implements EventDispatcher {

        public static final EventDispatcher INSTANCE = new EmptyEventDispatcher();

        private EmptyEventDispatcher() {}

        @Override
        public void dispatch(ComponentContainer container, Event event) {
            // NO-OP
        }

        @Override
        public List<EventListener> getListeners() {
            return List.of();
        }

    }

    private class SingleEventDispatcher implements EventDispatcher<T, E> {

        private final EventListener<E, ? extends T, ?> listener;
        private final BiConsumer consumer;
        private final int cIdx;

        private SingleEventDispatcher(EventListener<E, ? extends T, ?> eventListener) {
            this.listener = eventListener;
            this.consumer = eventListener.getConsumer();
            this.cIdx = eventListener.getComponentType().getInternalID();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void dispatch(ComponentContainer<?> container, E event) {
            final var component = container.getComponent(cIdx);
            if (component != null) {
                event.lIdx = 0;
                consumer.accept(component, event);
                event.lIdx = -1;
            }
        }

        @Override
        public List<EventListener<E, ? extends T, ?>> getListeners() {
            return List.of(listener);
        }

    }

    private class MultiEventDispatcher implements EventDispatcher<T, E> {

        private final List<EventListener<E, ? extends T, ?>> listeners;
        private final BiConsumer[] consumers;
        private final int[] cIdxs;

        private MultiEventDispatcher(List<EventListener<E, ? extends T, ?>> listeners) {
            this.listeners = listeners;
            this.consumers = new BiConsumer[listeners.size()];
            this.cIdxs = new int[listeners.size()];
            for (int i = 0; i < listeners.size(); i++) {
                this.consumers[i] = listeners.get(i).getConsumer();
                this.cIdxs[i] = listeners.get(i).getComponentType().getInternalID();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void dispatch(ComponentContainer<?> container, E event) {
            for (int i = 0; i < consumers.length; i++) {
                final var component = container.getComponent(cIdxs[i]);
                if (component != null) {
                    event.lIdx = i;
                    consumers[i].accept(component, event);
                } event.lIdx = -1;
            }
        }

        @Override
        public List<EventListener<E, ? extends T, ?>> getListeners() {
            return listeners;
        }

    }

}
