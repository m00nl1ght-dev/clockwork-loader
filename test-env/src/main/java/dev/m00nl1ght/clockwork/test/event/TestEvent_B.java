package dev.m00nl1ght.clockwork.test.event;

import dev.m00nl1ght.clockwork.events.EventType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventTypeTemporaryImpl;
import dev.m00nl1ght.clockwork.test.TestTarget_B;

public class TestEvent_B implements Event {

    public static final EventType<TestEvent_B, TestTarget_B> TYPE = new EventTypeTemporaryImpl<>(TestEvent_B.class, TestTarget_B.class);
    
}
