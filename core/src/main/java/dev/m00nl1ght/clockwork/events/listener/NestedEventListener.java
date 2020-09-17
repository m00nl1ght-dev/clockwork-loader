package dev.m00nl1ght.clockwork.events.listener;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventListenerPriority;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.function.BiConsumer;

public class NestedEventListener<E extends Event, T extends ComponentTarget, C extends ComponentTarget, I> extends EventListener<E, T, C> {

    private final ComponentType<I, C> innerComponentType;
    private final BiConsumer<I, E> innerConsumer;
    private final int cIdx;

    public NestedEventListener(TypeRef<E> eventClassType, ComponentType<C, T> outerComponentType,
                               ComponentType<I, C> innerComponentType, BiConsumer<I, E> innerConsumer,
                               EventListenerPriority priority) {
        super(eventClassType, outerComponentType, priority);
        this.innerComponentType = innerComponentType;
        this.cIdx = innerComponentType.getInternalIdx();
        this.innerConsumer = innerConsumer;
    }

    public NestedEventListener(Class<E> eventClass, ComponentType<C, T> outerComponentType,
                               ComponentType<I, C> innerComponentType, BiConsumer<I, E> innerConsumer,
                               EventListenerPriority priority) {
        this(TypeRef.of(eventClass), outerComponentType, innerComponentType, innerConsumer, priority);
    }

    @Override
    public BiConsumer<C, E> getConsumer() {
        return this::invoke;
    }

    private void invoke(C innerTarget, E event) {
        @SuppressWarnings("unchecked")
        final I innerComponent = (I) innerTarget.getComponent(cIdx);
        if (innerComponent != null) {
            innerConsumer.accept(innerComponent, event);
        }
    }

}
