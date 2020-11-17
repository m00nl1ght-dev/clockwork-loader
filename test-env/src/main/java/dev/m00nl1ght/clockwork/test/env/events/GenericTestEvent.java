package dev.m00nl1ght.clockwork.test.env.events;

import dev.m00nl1ght.clockwork.util.TypeRef;

public class GenericTestEvent<T> extends TestEvent {

    public static final TypeRef<GenericTestEvent<String>> STRING_TYPE = new TypeRef<>(){};

    public final T genericObject;

    public GenericTestEvent(T genericObject) {
        this.genericObject = genericObject;
    }

}
