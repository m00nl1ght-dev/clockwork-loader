package dev.m00nl1ght.clockwork.event.filter;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.EventFilter;

import java.lang.reflect.Method;

public interface EventFilterFactory {

    <E, T extends ComponentTarget<? super T>> EventFilter<E, T> get(ComponentType<?, T> componentType, Class<E> eventClass, Method method);

}
