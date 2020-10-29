package dev.m00nl1ght.clockwork.test.env.events;

import dev.m00nl1ght.clockwork.events.impl.ContextAwareEvent;

public class GenericTestEvent<T> extends ContextAwareEvent {

    public final T genericObject;

    public GenericTestEvent(T genericObject) {
        this.genericObject = genericObject;
    }

}
