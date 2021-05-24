package dev.m00nl1ght.clockwork.extension.annotations;

import dev.m00nl1ght.clockwork.core.*;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.events.listener.EventListenerPriority;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.TypeRef;
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
            @NotNull EventListenerPriority priority) {

        final var handlerClass = method.getDeclaringClass();
        final var params = method.getGenericParameterTypes();
        if (Modifier.isStatic(method.getModifiers())) {
            if (params.length != 2) return null;
            final var eventType = eventTypeRef(params[1]);
            if (!(params[0] instanceof Class) || eventType == null) return null;
            final var component = findComponentType(core, (Class<?>) params[0]);
            if (component == null) return null;
            return new EventHandlerMethod<>(eventType, component, priority, lookup, method);
        } else {
            if (params.length != 1) return null;
            final var eventType = eventTypeRef(params[0]);
            if (eventType == null) return null;
            final var component = findComponentType(core, handlerClass);
            if (component == null) return null;
            return new EventHandlerMethod<>(eventType, component, priority, lookup, method);
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
            @NotNull EventListenerPriority priority,
            @NotNull Lookup lookup,
            @NotNull Method method) {

        super(eventType, componentType, priority);
        this.lookup = lookup;
        this.method = method;
    }

    private @NotNull BiConsumer<C, E> buildConsumer() {
        // TODO this breaks in JVM 15+ because of new hidden classes mechanics
        try {
            final var handle = lookup.unreflect(method);
            final var callsite = LambdaMetafactory.metafactory(lookup,
                    "accept", MHBC_INVOKED_TYPE, MHBC_GENERIC_TYPE, handle, handle.type());
            @SuppressWarnings("unchecked")
            final var consumer = (BiConsumer<C, E>) callsite.getTarget().invokeExact();
            return consumer;
        } catch (Throwable t) {
            throw FormatUtil.rtExc(t, "Failed to build lambda for event handler [] using deep reflection", this);
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
    public String toString() {
        return componentType + "#" + method.getName();
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
