package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public interface StaticEventDispatcher<E extends Event, T extends ComponentTarget, O extends ComponentTarget> extends EventDispatcher<E, T> {

    // TODO rework?

    EventDispatcher<E, O> getOrigin();

    T getTarget();

    @Override
    default TypeRef<E> getEventClassType() {
        return getOrigin().getEventClassType();
    }

    @Override
    default Collection<TargetType<? extends T>> getCompatibleTargetTypes() {
        return List.of(getTargetType());
    }

    final class Key {

        public final TypeRef<?> eventType;
        public final Class<?> targetClass;
        public final Class<?> originClass;
        public final Object target;

        public Key(TypeRef<?> eventType, Class<?> targetClass, Class<?> originClass, Object target) {
            this.eventType = Objects.requireNonNull(eventType);
            this.targetClass = Objects.requireNonNull(targetClass);
            this.originClass = Objects.requireNonNull(originClass);
            this.target = Objects.requireNonNull(target);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key key = (Key) o;
            return eventType.equals(key.eventType) &&
                    targetClass == key.targetClass &&
                    originClass == key.originClass &&
                    target.equals(key.target);
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventType, targetClass, originClass, target);
        }

    }

}
