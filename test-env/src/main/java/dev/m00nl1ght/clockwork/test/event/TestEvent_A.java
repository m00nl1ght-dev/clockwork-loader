package dev.m00nl1ght.clockwork.test.event;

import dev.m00nl1ght.clockwork.events.EventType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventTypeTemporaryImpl;
import dev.m00nl1ght.clockwork.test.TestTarget_A;

public class TestEvent_A implements Event {

    public static final EventType<TestEvent_A, TestTarget_A> TYPE = new EventTypeTemporaryImpl<>(TestEvent_A.class, TestTarget_A.class);

}
