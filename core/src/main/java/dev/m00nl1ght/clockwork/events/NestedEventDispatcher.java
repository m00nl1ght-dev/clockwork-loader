package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Objects;

public interface NestedEventDispatcher<E extends Event, T extends ComponentTarget, O extends ComponentTarget> extends EventDispatcher<E, T> {

    EventDispatcher<E, O> getOrigin();

    ComponentType<T, O> getComponentOrigin();

    @Override
    default TypeRef<E> getEventClassType() {
        return getOrigin().getEventClassType();
    }

    final class Key {

        public final TypeRef<?> eventType;
        public final Class<?> targetClass;
        public final Class<?> originClass;

        public Key(TypeRef<?> eventType, Class<?> targetClass, Class<?> originClass) {
            this.eventType = Objects.requireNonNull(eventType);
            this.targetClass = Objects.requireNonNull(targetClass);
            this.originClass = Objects.requireNonNull(originClass);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key key = (Key) o;
            return eventType.equals(key.eventType) &&
                    targetClass == key.targetClass &&
                    originClass == key.originClass;
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventType, targetClass, originClass);
        }

    }

}
