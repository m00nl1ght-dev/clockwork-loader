package dev.m00nl1ght.clockwork.test.event;

import dev.m00nl1ght.clockwork.core.EventType;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.test.TestTarget_B;

public class TestEvent_B implements Event {

    public static final EventType<TestEvent_B, TestTarget_B> TYPE = TestTarget_B.TARGET_TYPE.getEventType(TestEvent_B.class);
    
}
