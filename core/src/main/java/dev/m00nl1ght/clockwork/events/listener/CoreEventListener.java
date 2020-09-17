package dev.m00nl1ght.clockwork.events.listener;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventListenerPriority;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.function.BiConsumer;

public class CoreEventListener<E extends Event, T extends ComponentTarget, I> extends EventListener<E, T, ClockworkCore> {

    private final ComponentType<I, ClockworkCore> innerComponentType;
    private final BiConsumer<I, E> innerConsumer;
    private final I innerComponent;

    public CoreEventListener(TypeRef<E> eventClassType, ComponentType<ClockworkCore, T> outerComponentType,
                             ComponentType<I, ClockworkCore> innerComponentType, BiConsumer<I, E> innerConsumer,
                             ClockworkCore core, EventListenerPriority priority) {
        super(eventClassType, outerComponentType, priority);
        core.getState().requireOrAfter(ClockworkCore.State.INITIALISED);
        this.innerComponentType = innerComponentType;
        this.innerComponent = innerComponentType.get(core);
        this.innerConsumer = innerConsumer;
    }

    public CoreEventListener(Class<E> eventClass, ComponentType<ClockworkCore, T> outerComponentType,
                             ComponentType<I, ClockworkCore> innerComponentType, BiConsumer<I, E> innerConsumer,
                             ClockworkCore core, EventListenerPriority priority) {
        this(TypeRef.of(eventClass), outerComponentType, innerComponentType, innerConsumer, core, priority);
    }

    @Override
    public BiConsumer<ClockworkCore, E> getConsumer() {
        return this::invoke;
    }

    private void invoke(ClockworkCore core, E event) {
        innerConsumer.accept(innerComponent, event);
    }

}
