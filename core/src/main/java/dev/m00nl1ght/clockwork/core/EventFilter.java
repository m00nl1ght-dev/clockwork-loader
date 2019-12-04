package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("Convert2streamapi")
public interface EventFilter<E, C, T extends ComponentTarget> {

    boolean test(E event, C component, T object);

    default EventFilter<E, C, T> and(EventFilter<E, C, T> other) {
        return new And<>(this, other);
    }

    class And<E, C, T extends ComponentTarget> implements EventFilter<E, C, T> {

        private final EventFilter<E, C, T> one, another;

        public And(EventFilter<E, C, T> one, EventFilter<E, C, T> another) {
            this.one = Preconditions.notNull(one, "one");
            this.another = Preconditions.notNull(another, "another");
        }

        @Override
        public boolean test(E event, C component, T object) {
            return one.test(event, component, object) && another.test(event, component, object);
        }

        @Override
        public EventFilter<E, C, T> and(EventFilter<E, C, T> other) {
            return new AndAll<>(List.of(one, another, other));
        }

        @Override
        public String toString() {
            return one.toString() + ", " + another.toString();
        }

    }

    class AndAll<E, C, T extends ComponentTarget> implements EventFilter<E, C, T> {

        private final List<EventFilter<E, C, T>> list;

        public AndAll(List<EventFilter<E, C, T>> list) {
            this.list = Preconditions.notNull(list, "list");
        }

        @Override
        public boolean test(E event, C component, T object) {
            for (var filter : list) if (!filter.test(event, component, object)) return false;
            return true;
        }

        @Override
        public EventFilter<E, C, T> and(EventFilter<E, C, T> other) {
            final var nl = new ArrayList<>(list);
            nl.add(other);
            return new AndAll<>(nl);
        }

        @Override
        public String toString() {
            return list.stream().map(Objects::toString).collect(Collectors.joining(", "));
        }

    }

}
