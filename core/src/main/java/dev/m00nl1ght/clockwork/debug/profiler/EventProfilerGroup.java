package dev.m00nl1ght.clockwork.debug.profiler;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventType;

import java.util.List;

public class EventProfilerGroup<E extends Event, T extends ComponentTarget> extends ProfilerGroup {

    protected final EventType<E, ? super T> eventType;
    protected final TargetType<T> targetType;
    protected EventProfilerEntry[] listenerEntries;

    public EventProfilerGroup(EventType<E, ? super T> eventType, TargetType<T> targetType) {
        this(eventType, targetType, 100);
    }

    public EventProfilerGroup(EventType<E, ? super T> eventType, TargetType<T> targetType, int bufferSize) {
        super(eventType.getEventClassType() + "@" + targetType.toString());
        this.eventType = eventType;
        this.targetType = targetType;
        final var listeners = eventType.getEffectiveListeners(targetType);
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

    public EventType<E, ? super T> getEventType() {
        return eventType;
    }

    public TargetType<T> getTargetType() {
        return targetType;
    }

    public EventProfilerGroup<E, T> attach() {
        eventType.attachProfiler(this);
        return this;
    }

}
