package dev.m00nl1ght.clockwork.benchmarks.event;

import dev.m00nl1ght.clockwork.benchmarks.TestEvent;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.BasicEventType;
import dev.m00nl1ght.clockwork.util.TypeRef;

public abstract class TestEventType<E extends TestEvent, T extends ComponentTarget> extends BasicEventType<E, T> {

    protected TestEventType(TypeRef<E> eventClassType, Class<T> targetClass) {
        super(eventClassType, targetClass);
    }

    protected TestEventType(Class<E> eventClass, Class<T> targetClass) {
        super(eventClass, targetClass);
    }

    protected TestEventType(TypeRef<E> eventClassType, TargetType<T> targetType) {
        super(eventClassType, targetType);
    }

    protected TestEventType(Class<E> eventClass, TargetType<T> targetType) {
        super(eventClass, targetType);
    }

    public abstract E postContextless(T object, E event);

}
