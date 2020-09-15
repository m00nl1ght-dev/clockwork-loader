package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class BasicEventTypeMulti<E extends Event, T extends ComponentTarget> extends BasicEventType<E, T> {

    protected final Map<Class<?>, LinkedEventType<E, ?, T>> linked = new LinkedHashMap<>();

    protected BasicEventTypeMulti(TypeRef<E> eventClassType, Class<T> targetClass) {
        super(eventClassType, targetClass);
    }

    protected BasicEventTypeMulti(Class<E> eventClass, Class<T> targetClass) {
        super(eventClass, targetClass);
    }

    protected BasicEventTypeMulti(TypeRef<E> eventClassType, TargetType<T> targetType) {
        super(eventClassType, targetType);
    }

    protected BasicEventTypeMulti(Class<E> eventClass, TargetType<T> targetType) {
        super(eventClass, targetType);
    }

    protected abstract void onLinkedListenersChanged(LinkedEventType<E, ?, T> linked);

    public <L extends ComponentTarget> void addLinkedTarget(Class<L> targetClass, Function<T, L> targetOrigin) {
        if (linked.putIfAbsent(targetClass, new LinkedEventType<>(this, targetClass, targetOrigin)) != null)
            throw FormatUtil.illStateExc("A linked target for class [] is already present", targetClass);
    }

    @SuppressWarnings("unchecked") // THC <L>
    public <L extends ComponentTarget> EventType<E, L> getLinkedTarget(Class<L> targetClass) {
        return (EventType<E, L>) linked.get(targetClass);
    }

    protected static class LinkedEventType<E extends Event, L extends ComponentTarget, R extends ComponentTarget> extends BasicEventTypeExact<E, L> {

        protected final BasicEventTypeMulti<E, R> root;
        protected final Function<R, L> targetOrigin;

        protected LinkedEventType(BasicEventTypeMulti<E, R> root, Class<L> targetClass, Function<R, L> targetOrigin) {
            super(root.getEventClassType(), targetClass);
            this.root = root;
            this.targetOrigin = targetOrigin;
        }

        @Override
        protected void onListenersChanged() {
            root.onLinkedListenersChanged(this);
        }

        @Override
        public E post(L object, E event) {
            throw new UnsupportedOperationException();
        }

    }

}
