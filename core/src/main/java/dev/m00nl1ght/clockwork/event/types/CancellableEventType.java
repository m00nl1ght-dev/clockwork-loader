package dev.m00nl1ght.clockwork.event.types;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentTargetType;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.event.EventType;
import dev.m00nl1ght.clockwork.event.EventTypeFactory;

import java.util.function.BiConsumer;

public class CancellableEventType<E extends CancellableEvent, T> extends EventType<E, T> {

    public CancellableEventType(ComponentTargetType<T> target, Class<E> eventClass) {
        super(target, eventClass);
    }

    @Override
    protected <C> Listener<C, E, T> buildListener(ComponentType<C, T> componentType, BiConsumer<C, E> consumer) {
        return new CancellableListener<>(componentType, consumer, false);
    }

    public static class Factory implements EventTypeFactory<CancellableEvent> {

        @Override
        public <T, E extends CancellableEvent> EventType<E, T> build(ComponentTargetType<T> targetType, Class<E> eventClass) {
            return new CancellableEventType<>(targetType, eventClass);
        }

        @Override
        public Class<CancellableEvent> getTarget() {
            return CancellableEvent.class;
        }

    }

    protected static class CancellableListener<C, E extends CancellableEvent, T> extends Listener<C, E, T> {

        private final boolean receiveCancelled;

        protected CancellableListener(ComponentType<C, T> component, BiConsumer<C, E> consumer, boolean receiveCancelled) {
            super(component, consumer);
            this.receiveCancelled = receiveCancelled;
        }

        @Override
        protected void accept(E event, ComponentTarget<T> object) {
            if (!event.isCancelled() || receiveCancelled) {
                final var comp = object.getComponent(component);
                if (comp != null) consumer.accept(comp, event);
            }
        }

    }

}
