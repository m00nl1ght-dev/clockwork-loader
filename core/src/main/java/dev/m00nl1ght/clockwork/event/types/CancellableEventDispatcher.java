package dev.m00nl1ght.clockwork.event.types;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentTargetType;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.EventDispatcher;
import dev.m00nl1ght.clockwork.core.EventDispatcherFactory;

import java.util.function.BiConsumer;

public class CancellableEventDispatcher<E extends CancellableEvent, T extends ComponentTarget> extends EventDispatcher<E, T> {

    public CancellableEventDispatcher(ComponentTargetType<T> target, Class<E> eventClass) {
        super(target, eventClass);
    }

    @Override
    protected <C> Listener<C, E, T> buildListener(ComponentType<C, T> componentType, BiConsumer<C, E> consumer) {
        return new CancellableListener<>(componentType, consumer, false);
    }

    public static class Factory implements EventDispatcherFactory<CancellableEvent> {

        @Override
        public <T extends ComponentTarget, E extends CancellableEvent> EventDispatcher<E, T> build(ComponentTargetType<T> targetType, Class<E> eventClass) {
            return new CancellableEventDispatcher<>(targetType, eventClass);
        }

        @Override
        public Class<CancellableEvent> getTarget() {
            return CancellableEvent.class;
        }

    }

    protected static class CancellableListener<C, E extends CancellableEvent, T extends ComponentTarget> extends Listener<C, E, T> {

        private final boolean receiveCancelled;

        protected CancellableListener(ComponentType<C, T> component, BiConsumer<C, E> consumer, boolean receiveCancelled) {
            super(component, consumer);
            this.receiveCancelled = receiveCancelled;
        }

        @Override
        protected void accept(E event, T object) {
            if (!event.isCancelled() || receiveCancelled) {
                final var comp = object.getComponent(component);
                if (comp != null) consumer.accept(comp, event);
            }
        }

    }

}
