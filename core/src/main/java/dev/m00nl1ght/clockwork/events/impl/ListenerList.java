package dev.m00nl1ght.clockwork.events.impl;

import dev.m00nl1ght.clockwork.debug.profiler.EventDispatcherProfilerGroup;
import dev.m00nl1ght.clockwork.events.listener.EventListener;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public class ListenerList {

    public static final ListenerList EMPTY = new ListenerList(Collections.emptyList());

    public final List<? extends EventListener<?, ?, ?>> listeners;
    public final BiConsumer[] consumers;
    public final int[] cIdxs;

    public ListenerList(List<? extends EventListener<?, ?, ?>> listeners) {
        this(listeners, null);
    }

    public ListenerList(List<? extends EventListener<?, ?, ?>> listeners, EventDispatcherProfilerGroup<?, ?> profilerGroup) {
        this.listeners = listeners;
        this.consumers = new BiConsumer[listeners.size()];
        this.cIdxs = new int[listeners.size()];
        if (profilerGroup != null) {
            for (int i = 0; i < listeners.size(); i++) {
                this.consumers[i] = profilerGroup.getEntry(i);
                this.cIdxs[i] = listeners.get(i).getComponentType().getInternalIdx();
            }
        } else {
            for (int i = 0; i < listeners.size(); i++) {
                this.consumers[i] = listeners.get(i).getConsumer();
                this.cIdxs[i] = listeners.get(i).getComponentType().getInternalIdx();
            }
        }
    }

}
