package dev.m00nl1ght.clockwork.test.event;

import dev.m00nl1ght.clockwork.events.EventDispatcher;
import dev.m00nl1ght.clockwork.events.impl.ContextAwareEvent;
import dev.m00nl1ght.clockwork.test.TestLauncher;
import dev.m00nl1ght.clockwork.test.TestTarget_A;

public class SimpleTestEvent extends ContextAwareEvent {

    public static final EventDispatcher<SimpleTestEvent, TestTarget_A> TYPE =
            TestLauncher.eventBus().getEventDispatcher(SimpleTestEvent.class, TestTarget_A.class);

}
