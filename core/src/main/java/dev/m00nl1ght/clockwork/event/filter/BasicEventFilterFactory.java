package dev.m00nl1ght.clockwork.event.filter;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.EventFilter;
import dev.m00nl1ght.clockwork.util.Preconditions;

import java.lang.reflect.Method;

public abstract class BasicEventFilterFactory<I> implements EventFilterFactory {

    private final Class<I> evtInterface;

    protected BasicEventFilterFactory(Class<I> evtInterface) {
        this.evtInterface = Preconditions.notNullAnd(evtInterface, Class::isInterface, "evtInterface");
    }

    @SuppressWarnings("unchecked")
    public <E, T extends ComponentTarget> EventFilter<E, T> get(ComponentType<?, T> componentType, Class<E> eventClass, Method method) {
        if (evtInterface.isAssignableFrom(eventClass)) {
            return (EventFilter<E, T>) build(componentType, (Class<? extends I>) eventClass, method);
        } else {
            return null;
        }
    }

    protected abstract <E extends I, T extends ComponentTarget> EventFilter<E, T> build(ComponentType<?, T> componentType, Class<E> eventClass, Method method);

}
