package dev.m00nl1ght.clockwork.events.impl;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventType;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public abstract class BasicEventType<E extends Event, T extends ComponentTarget> extends EventType<E, T> {

    protected final List[] listeners;
    protected final TargetType<? super T> rootTarget;
    protected final int idxOffset;

    protected BasicEventType(TypeRef<E> eventClassType, TargetType<T> targetType) {
        super(eventClassType, targetType);
        this.rootTarget = targetType.getRoot();
        this.idxOffset = targetType.getSubtargetIdxFirst();
        final var cnt = targetType.getSubtargetIdxLast() - idxOffset + 1;
        this.listeners = new List[cnt];
    }

    @Override
    public <S extends T> List<EventListener<E, S, ?>> getListeners(TargetType<S> target) {
        if (target.getRoot() != rootTarget) checkCompatibility(target);
        try {
            @SuppressWarnings("unchecked")
            final List<EventListener<E, S, ?>> list = listeners[target.getSubtargetIdxFirst() - idxOffset];
            if (list == null) return List.of();
            return list;
        } catch (Exception e) {
            checkCompatibility(target);
            throw e;
        }
    }

    @Override
    public void addListeners(Collection<EventListener<E, ? extends T, ?>> eventListeners) {
        final var modified = new HashSet<TargetType<? extends T>>();
        for (final var listener : eventListeners) {
            final var type = listener.getComponentType().getTargetType();
            if (type.getRoot() != rootTarget) checkCompatibility(type);
            final var idx = type.getSubtargetIdxFirst() - idxOffset;
            if (listeners[idx] == null) listeners[idx] = new ArrayList(5);
            @SuppressWarnings("unchecked") final var list = (List<EventListener<E, ? extends T, ?>>) listeners[idx];
            if (!list.contains(listener)) {
                list.add(listener);
                modified.addAll(type.getAllSubtargets());
            }
        }
        for (final var type : modified) {
            onListenersChanged(type);
        }
    }

    @Override
    public void removeListeners(Collection<EventListener<E, ? extends T, ?>> eventListeners) {
        final var modified = new HashSet<TargetType<? extends T>>();
        for (final var listener : eventListeners) {
            final var type = listener.getComponentType().getTargetType();
            if (type.getRoot() != rootTarget) checkCompatibility(type);
            final var idx = type.getSubtargetIdxFirst() - idxOffset;
            if (listeners[idx] == null) continue;
            final var list = listeners[idx];
            if (list.remove(listener)) {
                modified.addAll(type.getAllSubtargets());
            }
        }
        for (final var type : modified) {
            onListenersChanged(type);
        }
    }

    protected abstract void onListenersChanged(TargetType<? extends T> targetType);

}
