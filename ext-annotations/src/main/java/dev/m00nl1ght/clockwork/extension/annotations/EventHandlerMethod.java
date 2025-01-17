package dev.m00nl1ght.clockwork.extension.annotations;

import dev.m00nl1ght.clockwork.component.ComponentTarget;
import dev.m00nl1ght.clockwork.component.ComponentType;
import dev.m00nl1ght.clockwork.component.TargetType;
import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ClockworkException;
import dev.m00nl1ght.clockwork.core.Component;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.event.EventListener;
import dev.m00nl1ght.clockwork.utils.reflect.TypeRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.BiConsumer;

public final class EventHandlerMethod<E extends Event, T extends ComponentTarget, C> extends EventListener<E, T, C> {

    private static final MethodType MHBC_GENERIC_TYPE = MethodType.methodType(Void.TYPE, Object.class, Object.class);
    private static final MethodType MHBC_INVOKED_TYPE = MethodType.methodType(BiConsumer.class);

    private final Method method;
    private final Lookup lookup;

    private BiConsumer<C, E> cachedLambda;

    public static @Nullable EventHandlerMethod<?, ?, ?> build(
            @NotNull ClockworkCore core,
            @NotNull Method method,
            @NotNull Lookup lookup,
            @NotNull EventListener.Phase phase) {

        final var handlerClass = method.getDeclaringClass();
        final var params = method.getGenericParameterTypes();
        if (Modifier.isStatic(method.getModifiers())) {
            if (params.length != 2) return null;
            final var eventType = eventTypeRef(params[1]);
            if (!(params[0] instanceof Class) || eventType == null) return null;
            final var component = findComponentType(core, (Class<?>) params[0]);
            if (component == null) return null;
            return new EventHandlerMethod<>(eventType, component, phase, lookup, method);
        } else {
            if (params.length != 1) return null;
            final var eventType = eventTypeRef(params[0]);
            if (eventType == null) return null;
            final var component = findComponentType(core, handlerClass);
            if (component == null) return null;
            return new EventHandlerMethod<>(eventType, component, phase, lookup, method);
        }
    }

    private static @Nullable ComponentType<?, ?> findComponentType(
            @NotNull ClockworkCore core,
            @NotNull Class<?> forClass) {

        if (Component.class.isAssignableFrom(forClass)) {
            @SuppressWarnings("unchecked")
            final var regComp = core.getComponentType((Class<? extends Component<?>>) forClass);
            if (regComp.isPresent()) return regComp.get();
        }

        if (ComponentTarget.class.isAssignableFrom(forClass)) {
            @SuppressWarnings("unchecked")
            final var asTarget = core.getTargetType((Class<? extends ComponentTarget>) forClass);
            return asTarget.map(TargetType::getIdentityComponentType).orElse(null);
        }

        return null;
    }

    private static @Nullable TypeRef<? extends Event> eventTypeRef(@NotNull Type paramType) {
        if (paramType instanceof Class) {
            final var eventClass = (Class<?>) paramType;
            if (!Event.class.isAssignableFrom(eventClass)) return null;
            @SuppressWarnings("unchecked")
            final var castedClass = (Class<? extends Event>) eventClass;
            return TypeRef.of(castedClass);
        } else if (paramType instanceof ParameterizedType) {
            final var type = (ParameterizedType) paramType;
            if (!(type.getRawType() instanceof Class)) return null;
            final var rawType = (Class<?>) type.getRawType();
            if (!Event.class.isAssignableFrom(rawType)) return null;
            return TypeRef.of(type);
        } else {
            return null;
        }
    }

    private EventHandlerMethod(
            @NotNull TypeRef<E> eventType,
            @NotNull ComponentType<C, T> componentType,
            @NotNull EventListener.Phase phase,
            @NotNull Lookup lookup,
            @NotNull Method method) {

        super(eventType, componentType, phase);
        this.lookup = lookup;
        this.method = method;
    }

    private @NotNull BiConsumer<C, E> buildConsumer() {
        try {
            final var handle = lookup.unreflect(method);
            final var callsite = LambdaMetafactory.metafactory(lookup,
                    "accept", MHBC_INVOKED_TYPE, MHBC_GENERIC_TYPE, handle, handle.type());
            @SuppressWarnings("unchecked")
            final var consumer = (BiConsumer<C, E>) callsite.getTarget().invokeExact();
            return consumer;
        } catch (Throwable t) {
            throw ClockworkException.generic(componentType, t, "Failed to build lambda for event handler [] using deep reflection", this);
        }
    }

    public @NotNull Method getMethod() {
        return method;
    }

    @Override
    public @NotNull BiConsumer<C, E> getConsumer() {
        if (cachedLambda == null) cachedLambda = buildConsumer();
        return cachedLambda;
    }

    @Override
    public @NotNull String getUniqueID() {
        return method.getDeclaringClass().getName() + "#" + method.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventHandlerMethod)) return false;
        if (!super.equals(o)) return false;
        EventHandlerMethod<?, ?, ?> that = (EventHandlerMethod<?, ?, ?>) o;
        return method.equals(that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), method);
    }

}
