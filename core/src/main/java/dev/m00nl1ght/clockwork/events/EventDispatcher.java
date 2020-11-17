package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.EventDispatcherProfilerGroup;
import dev.m00nl1ght.clockwork.debug.profiler.Profilable;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Collection;
import java.util.List;

public interface EventDispatcher<E extends Event, T extends ComponentTarget> extends Profilable<EventDispatcherProfilerGroup<E, ? extends T>> {

    TypeRef<E> getEventType();

    TargetType<T> getTargetType();

    E post(T object, E event);

    <S extends T> List<EventListener<E, ? super S, ?>> getListeners(TargetType<S> target);

    <S extends T> EventListenerCollection<E, S> getListenerCollection(TargetType<S> target);

    <S extends T> void setListenerCollection(EventListenerCollection<E, S> collection);

    Collection<TargetType<? extends T>> getCompatibleTargetTypes();

}
