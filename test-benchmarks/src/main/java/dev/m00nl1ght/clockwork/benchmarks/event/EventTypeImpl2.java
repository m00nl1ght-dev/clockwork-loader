package dev.m00nl1ght.clockwork.benchmarks.event;

import dev.m00nl1ght.clockwork.benchmarks.TestEvent;
import dev.m00nl1ght.clockwork.core.ComponentContainer;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.EventListener;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.List;
import java.util.function.BiConsumer;

public class EventTypeImpl2<E extends TestEvent, T extends ComponentTarget> extends TestEventType<E, T> {

    private DispatchGroup[] groups;

    public EventTypeImpl2(TypeRef<E> eventClassType, Class<T> targetClass) {
        super(eventClassType, targetClass);
    }

    public EventTypeImpl2(Class<E> eventClass, Class<T> targetClass) {
        super(eventClass, targetClass);
    }

    public EventTypeImpl2(TypeRef<E> eventClassType, TargetType<T> targetType) {
        super(eventClassType, targetType);
    }

    public EventTypeImpl2(Class<E> eventClass, TargetType<T> targetType) {
        super(eventClass, targetType);
    }

    @Override
    protected void init() {
        super.init();
        final var cnt = getTargetType().getSubtargetIdxLast() - idxOffset + 1;
        this.groups = new DispatchGroup[cnt];
    }

    @Override
    protected void onListenersChanged(TargetType<? extends T> targetType) {
        final var listeners = getEffectiveListeners(targetType);
        final var idx = targetType.getSubtargetIdxFirst() - idxOffset;
        groups[idx] = new DispatchGroup<>(listeners);
    }

    @Override
    @SuppressWarnings("unchecked")
    public E post(T object, E event) {
        final ComponentContainer<?> container = object.getComponentContainer();
        final var target = container.getTargetType();
        if (target.getRoot() != rootTarget) checkCompatibility(target);
        try {
            final var group = groups[target.getSubtargetIdxFirst() - idxOffset];
            if (group == null) return event;
            event.dispatchGroup = group;
            for (int i = 0; i < group.consumers.length; i++) {
                final var component = container.getComponent(group.cIdxs[i]);
                if (component != null) {
                    event.lIdx = i;
                    group.consumers[i].accept(component, event);
                } event.lIdx = -1;
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
        final ComponentContainer<?> container = object.getComponentContainer();
        final var target = container.getTargetType();
        if (target.getRoot() != rootTarget) checkCompatibility(target);
        try {
            final var group = groups[target.getSubtargetIdxFirst() - idxOffset];
            if (group == null) return event;
            for (int i = 0; i < group.consumers.length; i++) {
                final var component = container.getComponent(group.cIdxs[i]);
                if (component != null) {
                    group.consumers[i].accept(component, event);
                }
            }
            return event;
        } catch (Throwable t) {
            checkCompatibility(target);
            throw t;
        }
    }

    public static class DispatchGroup<E extends TestEvent, T extends ComponentTarget> {

        private final List<EventListener<E, ? extends T, ?>> listeners;
        private final BiConsumer[] consumers;
        private final int[] cIdxs;

        DispatchGroup(List<EventListener<E, ? extends T, ?>> listeners) {
            this.listeners = listeners;
            this.consumers = new BiConsumer[listeners.size()];
            this.cIdxs = new int[listeners.size()];
            for (int i = 0; i < listeners.size(); i++) {
                this.consumers[i] = listeners.get(i).getConsumer();
                this.cIdxs[i] = listeners.get(i).getComponentType().getInternalIdx();
            }
        }

        public List<EventListener<E, ? extends T, ?>> getListeners() {
            return listeners;
        }

    }

}
