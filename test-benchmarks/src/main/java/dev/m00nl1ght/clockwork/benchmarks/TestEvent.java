package dev.m00nl1ght.clockwork.benchmarks;

import dev.m00nl1ght.clockwork.benchmarks.event.EventTypeImpl0;
import dev.m00nl1ght.clockwork.benchmarks.event.EventTypeImpl2;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventListener;

import java.util.List;

public class TestEvent implements Event {

    public EventTypeImpl0.EventDispatcher<?, ? extends TestEvent> dispatcher;
    public int lIdx = -1;

    public EventListener<?, ?, ?> listener;

    public EventTypeImpl2.DispatchGroup dispatchGroup;

    public List<EventListener> listeners;

    public EventTypeImpl0.EventDispatcher<?, ? extends TestEvent> getDispatcher() {
        return dispatcher;
    }

    public int getlIdx() {
        return lIdx;
    }

    public EventListener<?, ?, ?> getListener() {
        return listener;
    }

    public EventTypeImpl2.DispatchGroup getDispatchGroup() {
        return dispatchGroup;
    }

}
