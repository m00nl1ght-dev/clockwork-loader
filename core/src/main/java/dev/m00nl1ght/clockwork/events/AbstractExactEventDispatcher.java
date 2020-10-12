package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractExactEventDispatcher<E extends Event, T extends ComponentTarget> implements EventDispatcher<E, T> {

    protected final TypeRef<E> eventClassType;
    protected final TargetType<T> targetType;

    protected List listeners;

    protected AbstractExactEventDispatcher(TypeRef<E> eventClassType, TargetType<T> targetType) {
        this.eventClassType = Arguments.notNull(eventClassType, "eventClassType");
        this.targetType = Arguments.notNull(targetType, "targetType");
        targetType.requireInitialised();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends T> List<EventListener<E, S, ?>> getListeners(TargetType<S> target) {
        return getRawListeners(target);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<EventListener<E, ? extends T, ?>> getEffectiveListeners(TargetType<? extends T> target) {
        return getRawListeners(target);
    }

    protected List getRawListeners(TargetType<?> target) {
        if (target != targetType) checkCompatibility(target);
        if (listeners == null) return List.of();
        return listeners;
    }

    @Override
    public void addListeners(Collection<EventListener<E, ? extends T, ?>> eventListeners) {
        boolean modified = false;
        for (final var listener : eventListeners) {
            final var type = listener.getComponentType().getTargetType();
            if (type != targetType) checkCompatibility(type);
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
            if (type != targetType) checkCompatibility(type);
            if (listeners == null) continue;
            if (listeners.remove(listener)) {
                modified = true;
            }
        }
        if (modified) onListenersChanged();
    }

    protected abstract void onListenersChanged();

    @Override
    public TypeRef<E> getEventClassType() {
        return eventClassType;
    }

    @Override
    public TargetType<T> getTargetType() {
        return targetType;
    }

    @Override
    public Collection<TargetType<? extends T>> getCompatibleTargetTypes() {
        return List.of(targetType);
    }

    protected void checkCompatibility(TargetType<?> otherType) {
        if (otherType != targetType) {
            final var msg = "Cannot post event [] to different target []";
            throw new IllegalArgumentException(FormatUtil.format(msg, this, otherType));
        }
    }

    @Override
    public String toString() {
        return targetType == null ? eventClassType + "@?" : eventClassType + "@" + targetType;
    }

}
