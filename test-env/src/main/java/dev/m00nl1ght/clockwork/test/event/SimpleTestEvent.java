package dev.m00nl1ght.clockwork.test.event;

import dev.m00nl1ght.clockwork.events.impl.ContextAwareEvent;
import dev.m00nl1ght.clockwork.events.EventType;
import dev.m00nl1ght.clockwork.events.impl.EventTypeImpl;
import dev.m00nl1ght.clockwork.test.TestTarget_A;

public class SimpleTestEvent extends ContextAwareEvent {

    public static final EventType<SimpleTestEvent, TestTarget_A> TYPE =
            new EventTypeImpl<>(SimpleTestEvent.class, TestTarget_A.TARGET_TYPE);

}
