package dev.m00nl1ght.clockwork.event.filter;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.EventFilter;
import dev.m00nl1ght.clockwork.event.CancellableEvent;
import dev.m00nl1ght.clockwork.event.EventHandler;

import java.lang.reflect.Method;

public class CancellableEventFilterFactory extends BasicEventFilterFactory<CancellableEvent> {

    public static final EventFilterFactory INSTANCE = new CancellableEventFilterFactory();

    private CancellableEventFilterFactory() {
        super(CancellableEvent.class);
    }

    @Override
    protected <E extends CancellableEvent, T extends ComponentTarget> EventFilter<E, T> build(ComponentType<?, T> componentType, Class<E> eventClass, Method method) {
        final var ann = method.getAnnotation(EventHandler.class);
        if (ann == null || !ann.receiveCancelled) {
            return new Filter<>();
        } else {
            return null;
        }
    }

    private static class Filter<E extends CancellableEvent, T extends ComponentTarget> implements EventFilter<E, T> {

        @Override
        public boolean test(E event, T object) {
            return !event.isCancelled();
        }

        @Override
        public String toString() {
            return "Uncancelled";
        }

    }

}
