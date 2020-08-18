package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public abstract class BasicEventType<E extends Event, T extends ComponentTarget> extends EventType<E, T> {

    protected List[] listeners;
    protected TargetType<? super T> rootTarget;
    protected int idxOffset;

    protected BasicEventType(TypeRef<E> eventClassType, Class<T> targetClass) {
        super(eventClassType, targetClass);
    }

    protected BasicEventType(Class<E> eventClass, Class<T> targetClass) {
        super(eventClass, targetClass);
    }

    protected BasicEventType(TypeRef<E> eventClassType, TargetType<T> targetType) {
        super(eventClassType, targetType);
    }

    protected BasicEventType(Class<E> eventClass, TargetType<T> targetType) {
        super(eventClass, targetType);
    }

    @Override
    protected void init() {
        this.rootTarget = getTargetType().getRoot();
        this.idxOffset = getTargetType().getSubtargetIdxFirst();
        final var cnt = getTargetType().getSubtargetIdxLast() - idxOffset;
        this.listeners = new List[cnt];
    }

    @Override
    public <S extends T> List<EventListener<E, S, ?>> getListeners(TargetType<S> target) {
        if (target.getRoot() != rootTarget) target.checkCompatibility(this);
        try {
            @SuppressWarnings("unchecked")
            final List<EventListener<E, S, ?>> list = listeners[target.getSubtargetIdxFirst() - idxOffset];
            if (list == null) return List.of();
            return list;
        } catch (Exception e) {
            target.checkCompatibility(this);
            throw e;
        }
    }

    @Override
    public void addListeners(Iterable<EventListener<E, ? extends T, ?>> eventListeners) {
        final var modified = new HashSet<TargetType<? extends T>>();
        for (final var listener : eventListeners) {
            final var type = listener.getComponentType().getTargetType();
            if (type.getRoot() != rootTarget) type.checkCompatibility(this);
            final var idx = type.getSubtargetIdxFirst() - idxOffset;
            if (listeners[idx] == null) listeners[idx] = new ArrayList(5);
            @SuppressWarnings("unchecked") final var list = (List<EventListener<E, ? extends T, ?>>) listeners[idx];
            if (!list.contains(listener)) {
                list.add(listener);
                forTargetAndParents(type, modified::add);
            }
        }
        for (final var type : modified) {
            onListenersChanged(type);
        }
    }

    @Override
    public void removeListeners(Iterable<EventListener<E, ? extends T, ?>> eventListeners) {
        final var modified = new HashSet<TargetType<? extends T>>();
        for (final var listener : eventListeners) {
            final var type = listener.getComponentType().getTargetType();
            if (type.getRoot() != rootTarget) type.checkCompatibility(this);
            final var idx = type.getSubtargetIdxFirst() - idxOffset;
            if (listeners[idx] == null) continue;
            final var list = listeners[idx];
            if (list.remove(listener)) {
                forTargetAndParents(type, modified::add);
            }
        }
        for (final var type : modified) {
            onListenersChanged(type);
        }
    }

    protected abstract void onListenersChanged(TargetType<? extends T> targetType);

}
