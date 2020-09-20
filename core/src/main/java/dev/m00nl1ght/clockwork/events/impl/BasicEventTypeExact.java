package dev.m00nl1ght.clockwork.events.impl;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventType;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BasicEventTypeExact<E extends Event, T extends ComponentTarget> extends EventType<E, T> {

    protected List listeners;

    protected BasicEventTypeExact(TypeRef<E> eventClassType, TargetType<T> targetType) {
        super(eventClassType, targetType);
    }

    @SuppressWarnings("unchecked")
    protected List<EventListener<E, T, ?>> getListeners() {
        return listeners;
    }

    @Override
    public <S extends T> List<EventListener<E, S, ?>> getListeners(TargetType<S> target) {
        if (target != getTargetType()) checkCompatibility(target);
        try {
            @SuppressWarnings("unchecked")
            final List<EventListener<E, S, ?>> list = listeners;
            if (list == null) return List.of();
            return list;
        } catch (Exception e) {
            checkCompatibility(target);
            throw e;
        }
    }

    @Override
    public void addListeners(Collection<EventListener<E, ? extends T, ?>> eventListeners) {
        boolean modified = false;
        for (final var listener : eventListeners) {
            final var type = listener.getComponentType().getTargetType();
            if (type != getTargetType()) checkCompatibility(type);
            if (listeners == null) listeners = new ArrayList(5);
            @SuppressWarnings("unchecked")
            final var list = (List<EventListener<E, ? extends T, ?>>) listeners;
            if (!list.contains(listener)) {
                list.add(listener);
                modified = true;
            }
        }
        if (modified) onListenersChanged();
    }

    @Override
    public void removeListeners(Collection<EventListener<E, ? extends T, ?>> eventListeners) {
        boolean modified = false;
        for (final var listener : eventListeners) {
            final var type = listener.getComponentType().getTargetType();
            if (type != getTargetType()) checkCompatibility(type);
            if (listeners == null) continue;
            if (listeners.remove(listener)) {
                modified = true;
            }
        }
        if (modified) onListenersChanged();
    }

    @Override
    protected void checkCompatibility(TargetType<?> otherType) {
        if (getTargetType() == null) {
            final var msg = "Event type for [] is not registered";
            throw new IllegalArgumentException(FormatUtil.format(msg, eventClassType.getType().getTypeName()));
        } else if (otherType != getTargetType()) {
            final var msg = "Cannot post event [] to different target []";
            throw new IllegalArgumentException(FormatUtil.format(msg, this, otherType));
        }
    }

    protected abstract void onListenersChanged();

}
