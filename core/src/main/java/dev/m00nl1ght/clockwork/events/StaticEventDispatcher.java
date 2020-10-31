package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public interface StaticEventDispatcher<E extends Event, T extends ComponentTarget, O extends ComponentTarget> extends EventDispatcher<E, T> {

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

    class Key extends EventDispatcher.Key {

        public final Class<?> originClass;
        public final Object target;

        public Key(TypeRef<?> eventType, Class<?> targetClass, Class<?> originClass, Object target) {
            super(eventType, targetClass);
            this.originClass = Objects.requireNonNull(originClass);
            this.target = Objects.requireNonNull(target);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            if (!super.equals(o)) return false;
            Key key = (Key) o;
            return originClass == key.originClass &&
                    target.equals(key.target);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), originClass, target);
        }

    }

}
