package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.util.Arguments;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public abstract class AbstractEventDispatcher<E extends Event, T extends ComponentTarget> implements EventDispatcher<E, T> {

    protected final TypeRef<E> eventClassType;
    protected final TargetType<T> targetType;

    protected final List[] listeners;
    protected final TargetType<? super T> rootTarget;
    protected final int idxOffset;

    protected AbstractEventDispatcher(TypeRef<E> eventClassType, TargetType<T> targetType) {
        this.eventClassType = Arguments.notNull(eventClassType, "eventClassType");
        this.targetType = Arguments.notNull(targetType, "targetType");
        targetType.requireInitialised();
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
    public List<EventListener<E, ? extends T, ?>> getEffectiveListeners(TargetType<? extends T> target) {
        if (target.getRoot() != rootTarget) checkCompatibility(target);
        try {
            final var list = new ArrayList<EventListener<E, ? extends T, ?>>();
            TargetType<?> type = target;
            while (type != null) {
                @SuppressWarnings("unchecked")
                final List<EventListener<E, ? extends T, ?>> got = listeners[target.getSubtargetIdxFirst() - idxOffset];
                if (got != null) list.addAll(got);
                if (type == this.targetType) break;
                final var castedType = type.getParent();
                type = castedType;
            }
            list.sort(EventListener.PRIORITY_ORDER);
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
        return targetType.getAllSubtargets();
    }

    protected void checkCompatibility(TargetType<?> otherType) {
        if (!otherType.isEquivalentTo(targetType)) {
            final var msg = "Cannot post event [] to different target []";
            throw new IllegalArgumentException(FormatUtil.format(msg, this, otherType));
        }
    }

    @Override
    public String toString() {
        return targetType == null ? eventClassType + "@?" : eventClassType + "@" + targetType;
    }

}