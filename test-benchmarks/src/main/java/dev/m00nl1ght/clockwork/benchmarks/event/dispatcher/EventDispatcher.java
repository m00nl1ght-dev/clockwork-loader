package dev.m00nl1ght.clockwork.benchmarks.event.dispatcher;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.events.Event;

import java.util.List;
import java.util.function.BiConsumer;

public interface EventDispatcher<T extends ComponentTarget, E extends Event> {

    <C> EventDispatcher<T, E> addListener(ComponentType<C, T> componentType, BiConsumer<C, E> listener);

    <C> EventDispatcher<T, E> removeListener(ComponentType<C, T> componentType);

    List<ComponentType<?, ? super T>> getListeners();

    void post(T target, E event);

}
