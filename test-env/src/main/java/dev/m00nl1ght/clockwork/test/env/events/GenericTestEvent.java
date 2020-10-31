package dev.m00nl1ght.clockwork.test.env.events;

public class GenericTestEvent<T> extends TestEvent {

    public final T genericObject;

    public GenericTestEvent(T genericObject) {
        this.genericObject = genericObject;
    }

}
