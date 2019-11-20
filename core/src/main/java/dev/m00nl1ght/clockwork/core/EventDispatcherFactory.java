package dev.m00nl1ght.clockwork.core;

public interface EventDispatcherFactory {

    EventDispatcherFactory DEFAULT = new Default();

    <E, T extends ComponentTarget<? super T>> EventDispatcher<E, T> build(TargetType<T> targetType, Class<E> eventClass);

    class Default implements EventDispatcherFactory {

        private Default() {}

        @Override
        public <E, T extends ComponentTarget<? super T>> EventDispatcher<E, T> build(TargetType<T> targetType, Class<E> eventClass) {
            return new EventDispatcher<>(targetType, eventClass);
        }

    }

}
