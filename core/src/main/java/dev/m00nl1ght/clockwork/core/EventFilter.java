package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Convert2streamapi")
public interface EventFilter<E, T extends ComponentTarget> {

    boolean test(E event, T object);

    default EventFilter<E, T> and(EventFilter<E, T> other) {
        return new And<>(this, other);
    }

    class And<E, T extends ComponentTarget> implements EventFilter<E, T> {

        private final EventFilter<E, T> one, another;

        public And(EventFilter<E, T> one, EventFilter<E, T> another) {
            this.one = Preconditions.notNull(one, "one");
            this.another = Preconditions.notNull(another, "another");
        }

        @Override
        public boolean test(E event, T object) {
            return one.test(event, object) && another.test(event, object);
        }

        @Override
        public EventFilter<E, T> and(EventFilter<E, T> other) {
            return new AndAll<>(List.of(one, another, other));
        }

    }

    class AndAll<E, T extends ComponentTarget> implements EventFilter<E, T> {

        private final List<EventFilter<E, T>> list;

        public AndAll(List<EventFilter<E, T>> list) {
            this.list = Preconditions.notNull(list, "list");
        }

        @Override
        public boolean test(E event, T object) {
            for (var filter : list) if (!filter.test(event, object)) return false;
            return true;
        }

        @Override
        public EventFilter<E, T> and(EventFilter<E, T> other) {
            final var nl = new ArrayList<>(list);
            nl.add(other);
            return new AndAll<>(nl);
        }

    }

}
