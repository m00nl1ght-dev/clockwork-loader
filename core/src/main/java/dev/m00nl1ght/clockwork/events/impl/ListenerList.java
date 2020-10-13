package dev.m00nl1ght.clockwork.events.impl;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.debug.profiler.EventDispatcherProfilerGroup;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.listener.EventListener;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public class ListenerList<E extends Event, T extends ComponentTarget> {

    private static final ListenerList EMPTY = new ListenerList<>(Collections.emptyList(), null);

    @SuppressWarnings("unchecked")
    public static <E extends Event, T extends ComponentTarget> ListenerList<E, T> empty() {
        return (ListenerList<E, T>) EMPTY;
    }

    public final List<? extends EventListener<E, ? super T, ?>> listeners;
    public final BiConsumer[] consumers;
    public final int[] cIdxs;

    public ListenerList(List<? extends EventListener<E, ? super T, ?>> listeners, EventDispatcherProfilerGroup<E, T> profilerGroup) {
        this.listeners = listeners;
        this.consumers = new BiConsumer[listeners.size()];
        this.cIdxs = new int[listeners.size()];
        if (profilerGroup != null) {
            for (int i = 0; i < listeners.size(); i++) {
                final var listener = listeners.get(i);
                this.consumers[i] = profilerGroup.getEntry(listener);
                this.cIdxs[i] = listener.getComponentType().getInternalIdx();
            }
        } else {
            for (int i = 0; i < listeners.size(); i++) {
                final var listener = listeners.get(i);
                this.consumers[i] = listener.getConsumer();
                this.cIdxs[i] = listener.getComponentType().getInternalIdx();
            }
        }
    }

}
