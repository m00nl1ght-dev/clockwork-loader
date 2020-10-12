package dev.m00nl1ght.clockwork.debug.profiler;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventDispatcher;

import java.util.List;

public class EventDispatcherProfilerGroup<E extends Event, T extends ComponentTarget> extends ProfilerGroup {

    protected final EventDispatcher<E, ? super T> eventDispatcher;
    protected final TargetType<T> targetType;
    protected EventProfilerEntry[] listenerEntries;

    public EventDispatcherProfilerGroup(EventDispatcher<E, ? super T> eventDispatcher, TargetType<T> targetType) {
        this(eventDispatcher, targetType, 100);
    }

    public EventDispatcherProfilerGroup(EventDispatcher<E, ? super T> eventDispatcher, TargetType<T> targetType, int bufferSize) {
        super(eventDispatcher.getEventClassType() + "@" + targetType.toString());
        this.eventDispatcher = eventDispatcher;
        this.targetType = targetType;
        final var listeners = eventDispatcher.getEffectiveListeners(targetType);
        this.listenerEntries = new EventProfilerEntry[listeners.size()];
        for (int i = 0; i < listeners.size(); i++) {
            listenerEntries[i] = new EventProfilerEntry<>(listeners.get(i), bufferSize);
        }
    }

    @Override
    public List<ProfilerEntry> getEntries() {
        return List.of(listenerEntries);
    }

    public EventProfilerEntry<?, ?, ?> getEntry(int idx) {
        return listenerEntries[idx];
    }

    public EventDispatcher<E, ? super T> getEventType() {
        return eventDispatcher;
    }

    public TargetType<T> getTargetType() {
        return targetType;
    }

    public EventDispatcherProfilerGroup<E, T> attach() {
        eventDispatcher.attachProfiler(this);
        return this;
    }

}
