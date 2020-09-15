package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.debug.profiler.EventProfilerGroup;

import java.util.Collections;
import java.util.List;

public class ListenerListMulti extends ListenerList {

    public static final ListenerListMulti EMPTY = new ListenerListMulti(Collections.emptyList());

    public ListenerListMulti(List<? extends EventListener<?, ?, ?>> listeners) {
        super(listeners);
    }

    public ListenerListMulti(List<? extends EventListener<?, ?, ?>> listeners, EventProfilerGroup<?, ?> profilerGroup) {
        super(listeners, profilerGroup);
    }

}
