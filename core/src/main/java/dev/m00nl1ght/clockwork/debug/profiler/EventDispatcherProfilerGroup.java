package dev.m00nl1ght.clockwork.debug.profiler;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.event.EventDispatcher;
import dev.m00nl1ght.clockwork.event.EventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventDispatcherProfilerGroup<E extends Event, T extends ComponentTarget> extends ProfilerGroup {

    protected final EventDispatcher<E, ? super T> eventDispatcher;
    protected final TargetType<T> targetType;
    protected final int bufferSize;

    protected final Map<EventListener<E, ?, ?>, EventProfilerEntry<E, ?, ?>> entryMap = new HashMap<>();

    public EventDispatcherProfilerGroup(EventDispatcher<E, ? super T> eventDispatcher, TargetType<T> targetType) {
        this(eventDispatcher, targetType, 100);
    }

    public EventDispatcherProfilerGroup(EventDispatcher<E, ? super T> eventDispatcher, TargetType<T> targetType, int bufferSize) {
        super(eventDispatcher.getEventType() + "@" + targetType.toString());
        this.eventDispatcher = eventDispatcher;
        this.targetType = targetType;
        this.bufferSize = bufferSize;
    }

    @SuppressWarnings("unchecked")
    public <C, L extends ComponentTarget>
    EventProfilerEntry<E, L, C> getEntry(EventListener<E, L, C> listener) {
        return (EventProfilerEntry<E, L, C>) entryMap.computeIfAbsent(listener, l -> new EventProfilerEntry<>(l, bufferSize));
    }

    @Override
    public List<ProfilerEntry> getEntries() {
        return List.copyOf(entryMap.values());
    }

    public EventDispatcher<E, ? super T> getEventType() {
        return eventDispatcher;
    }

    public TargetType<T> getTargetType() {
        return targetType;
    }

}
