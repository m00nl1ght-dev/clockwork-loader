package dev.m00nl1ght.clockwork.core;

import java.util.function.BiConsumer;

public abstract class EventListener<E, T extends ComponentTarget> {

    private EventListener() {}

    public abstract void accept(T object, E event);

    public static class Base<C, E, T extends ComponentTarget> extends EventListener<E, T> {

        protected final ComponentType<C, T> component;
        protected final BiConsumer<C, E> consumer;

        protected Base(ComponentType<C, T> component, BiConsumer<C, E> consumer) {
            this.component = component;
            this.consumer = consumer;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void accept(T object, E event) {
            final var comp = object.getComponent(component.getInternalID());
            if (comp != null) consumer.accept((C) comp, event);
        }

        public ComponentType<C, T> getComponentType() {
            return component;
        }

        public BiConsumer<C, E> getConsumer() {
            return consumer;
        }

        public EventFilter<E, T> getFilter() {
            return null;
        }

    }

    public static class Filtered<C, E, T extends ComponentTarget> extends Base<C, E, T> {

        protected final EventFilter<E, T> filter;

        protected Filtered(ComponentType<C, T> component, BiConsumer<C, E> consumer, EventFilter<E, T> filter) {
            super(component, consumer);
            this.filter = filter;
        }

        @Override
        public void accept(T object, E event) {
            if (filter.test(event, object)) {
                super.accept(object, event);
            }
        }

        @Override
        public EventFilter<E, T> getFilter() {
            return filter;
        }

    }

    static class Compound<E, T extends ComponentTarget> extends EventListener<E, T> {

        private final EventListener[] listeners;
        private final TargetType<T> targetType;

        protected Compound(EventListener[] listeners, TargetType<T> targetType) {
            this.listeners = listeners;
            this.targetType = targetType;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void accept(T object, E event) {
            for (var listener : listeners) {
                try {
                    listener.accept(object, event);
                } catch (ExceptionInPlugin e) {
                    throw e;
                } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
                    targetType.checkCompatibility(object.getTargetType());
                    throw e;
                } catch (Throwable t) {
                    if (listener instanceof Base) {
                        final var base = (Base) listener;
                        throw ExceptionInPlugin.inEventHandler(base.getComponentType(), event, object, t);
                    } else {
                        throw t;
                    }
                }
            }
        }

    }

}
