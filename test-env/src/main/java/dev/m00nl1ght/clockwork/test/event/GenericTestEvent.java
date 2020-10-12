package dev.m00nl1ght.clockwork.test.event;

import dev.m00nl1ght.clockwork.events.EventDispatcher;
import dev.m00nl1ght.clockwork.events.impl.ContextAwareEvent;
import dev.m00nl1ght.clockwork.test.TestLauncher;
import dev.m00nl1ght.clockwork.test.TestTarget_B;
import dev.m00nl1ght.clockwork.util.TypeRef;

public class GenericTestEvent<T> extends ContextAwareEvent {

    public static final EventDispatcher<GenericTestEvent<String>, TestTarget_B> TYPE_STRING =
            TestLauncher.eventBus().getEventDispatcher(new TypeRef<>() {}, TestTarget_B.class);

    public static final EventDispatcher<GenericTestEvent, TestTarget_B> TYPE_RAW =
            TestLauncher.eventBus().getEventDispatcher(GenericTestEvent.class, TestTarget_B.class);

}
