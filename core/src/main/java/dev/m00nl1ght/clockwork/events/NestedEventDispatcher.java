package dev.m00nl1ght.clockwork.events;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Collection;
import java.util.List;

public interface NestedEventDispatcher<E extends Event, T extends ComponentTarget, O extends ComponentTarget> extends EventDispatcher<E, T> {

    EventDispatcher<E, O> getOrigin();

    ComponentType<T, O> getComponentOrigin();

    @Override
    default TypeRef<E> getEventClassType() {
        return getOrigin().getEventClassType();
    }

    @Override
    default Collection<TargetType<? extends T>> getCompatibleTargetTypes() {
        return List.of(getTargetType());
    }

}
