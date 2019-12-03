package dev.m00nl1ght.clockwork.test.event;

import dev.m00nl1ght.clockwork.core.EventType;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.test.TestTarget_A;

public class TestEvent_A implements Event {

    public static final EventType<TestEvent_A, TestTarget_A> TYPE = TestTarget_A.TARGET_TYPE.getEventType(TestEvent_A.class);

}
