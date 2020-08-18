package dev.m00nl1ght.clockwork.benchmarks;

import dev.m00nl1ght.clockwork.benchmarks.event.EventTypeImpl0;
import dev.m00nl1ght.clockwork.events.Event;

public class TestEvent implements Event {

    public EventTypeImpl0.EventDispatcher<?, ? extends TestEvent> dispatcher;
    public int lIdx = -1;

    public EventTypeImpl0.EventDispatcher<?, ? extends TestEvent> getDispatcher() {
        return dispatcher;
    }

    public int getlIdx() {
        return lIdx;
    }

}
