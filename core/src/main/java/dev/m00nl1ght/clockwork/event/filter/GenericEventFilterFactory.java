package dev.m00nl1ght.clockwork.event.filter;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.EventFilter;
import dev.m00nl1ght.clockwork.event.GenericEvent;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class GenericEventFilterFactory extends BasicEventFilterFactory<GenericEvent> {

    public static final EventFilterFactory INSTANCE = new GenericEventFilterFactory();

    private GenericEventFilterFactory() {
        super(GenericEvent.class);
    }

    @Override
    protected <E extends GenericEvent, C, T extends ComponentTarget> EventFilter<E, C, T> build(ComponentType<C, T> componentType, Class<E> eventClass, Method method) {
        final var type = method.getGenericParameterTypes()[0];
        if (type instanceof ParameterizedType) {
            var generic = ((ParameterizedType)type).getActualTypeArguments()[0];
            if (generic instanceof ParameterizedType) {
                return new Filter<>(((ParameterizedType)generic).getRawType());
            } else {
                return new Filter<>(generic);
            }
        } else {
            return null;
        }
    }

    private static class Filter<E extends GenericEvent, C, T extends ComponentTarget> implements EventFilter<E, C, T> {

        private final Type type;

        private Filter(Type type) {
            this.type = type;
        }

        @Override
        public boolean test(E event, C component, T object) {
            return event.getType() == type;
        }

        @Override
        public String toString() {
            return "<" + type.getTypeName() + ">";
        }

    }

}
