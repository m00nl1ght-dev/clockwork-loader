package dev.m00nl1ght.clockwork.test.event;

import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventType;
import dev.m00nl1ght.clockwork.events.EventTypeImpl;
import dev.m00nl1ght.clockwork.test.TestTarget_B;
import dev.m00nl1ght.clockwork.util.TypeRef;

public class GenericTestEvent<T> extends Event {

    public static final EventType<GenericTestEvent<String>, TestTarget_B> TYPE_STRING = new EventTypeImpl<>(new TypeRef<>() {}, TestTarget_B.class);
    public static final EventType<GenericTestEvent, TestTarget_B> TYPE_RAW = new EventTypeImpl<>(GenericTestEvent.class, TestTarget_B.class);

}
