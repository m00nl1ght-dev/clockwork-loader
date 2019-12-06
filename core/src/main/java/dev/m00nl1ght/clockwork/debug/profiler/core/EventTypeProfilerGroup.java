package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.EventType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.ProfilerEntry;
import dev.m00nl1ght.clockwork.debug.profiler.ProfilerGroup;

import java.util.List;

public class EventTypeProfilerGroup<T extends ComponentTarget> extends ProfilerGroup {

    protected final TargetType<T> targetType;
    protected EventProfilerEntry[] listenerEntries;

    public EventTypeProfilerGroup(String name, TargetType<T> targetType) {
        super(name);
        this.targetType = targetType;
    }

    protected void init(EventType<?, T> eventType) {
        final var listeners = eventType.getListeners(targetType);
        this.listenerEntries = new EventProfilerEntry[listeners.size()];
        for (int i = 0; i < listeners.size(); i++) {
            listenerEntries[i] = new EventProfilerEntry<>(eventType, listeners.get(i));
        }
    }

    @Override
    public List<ProfilerEntry> getEntries() {
        return List.of(listenerEntries);
    }

    @SuppressWarnings("unchecked")
    public EventProfilerEntry<T> get(int idx) {
        return listenerEntries[idx];
    }

    public static class WithParent<T extends ComponentTarget> extends EventTypeProfilerGroup<T> {

        private final EventTypeProfilerGroup<? super T> parent;

        public WithParent(String name, TargetType<T> targetType, EventTypeProfilerGroup<? super T> parent) {
            super(name, targetType);
            this.parent = parent;
        }

        @Override
        protected void init(EventType<?, T> eventType) {
            final var listeners = eventType.getListeners(targetType);
            this.listenerEntries = new EventProfilerEntry[listeners.size()];
            final var splitIdx = parent.listenerEntries.length;
            for (int i = 0; i < splitIdx; i++) {
                listenerEntries[i] = new EventProfilerEntry.WithParent<>(eventType, listeners.get(i), parent.get(i));
            }
            for (int i = splitIdx; i < listenerEntries.length; i++) {
                listenerEntries[i] = new EventProfilerEntry<>(eventType, listeners.get(i));
            }
        }

    }

}
