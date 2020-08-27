package dev.m00nl1ght.clockwork.extension.annotations.eventhandler;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventListener;
import dev.m00nl1ght.clockwork.events.EventListenerPriority;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.function.BiConsumer;

public final class EventHandlerMethod<E extends Event, C> {

    private static final MethodType MHBC_GENERIC_TYPE = MethodType.methodType(Void.TYPE, Object.class, Object.class);
    private static final MethodType MHBC_INVOKED_TYPE = MethodType.methodType(BiConsumer.class);

    private final Method method;
    private final Class<C> componentClass;
    private final TypeRef<E> eventClassType;
    private final EventListenerPriority priority;
    private final MethodHandles.Lookup lookup;

    private BiConsumer<C, E> cachedLambda;

    public static EventHandlerMethod<?, ?> build(MethodHandles.Lookup lookup, Method method, EventListenerPriority priority) {
        if (Modifier.isStatic(method.getModifiers())) return null;
        final var componentClass = method.getDeclaringClass();
        final var params = method.getGenericParameterTypes();
        if (params.length != 1) return null;
        if (params[0] instanceof Class) {
            final var eventClass = (Class<?>) params[0];
            if (!Event.class.isAssignableFrom(eventClass)) return null;
            @SuppressWarnings("unchecked") final var castedClass = (Class<? extends Event>) eventClass;
            return new EventHandlerMethod<>(lookup, method, componentClass, TypeRef.of(castedClass), priority);
        } else if (params[0] instanceof ParameterizedType) { // TODO test
            final var type = (ParameterizedType) params[0];
            if (!(type.getRawType() instanceof Class)) return null;
            final var rawType = (Class<?>) type.getRawType();
            if (!Event.class.isAssignableFrom(rawType)) return null;
            return new EventHandlerMethod<>(lookup, method, componentClass, TypeRef.of(type), priority);
        } else {
            return null;
        }
    }

    private EventHandlerMethod(MethodHandles.Lookup lookup, Method method, Class<C> componentClass, TypeRef<E> eventClassType, EventListenerPriority priority) {
        this.lookup = lookup;
        this.method = method;
        this.componentClass = componentClass;
        this.eventClassType = eventClassType;
        this.priority = priority;
    }

    public <T extends ComponentTarget> EventListener<E, T, C> buildListener(ComponentType<C, T> componentType) {
        if (cachedLambda == null) cachedLambda = buildConsumer();
        return new EventListener<>(eventClassType, componentType, priority, cachedLambda);
    }

    private BiConsumer<C, E> buildConsumer() {
        try {
            final var handle = lookup.unreflect(method);
            final var callsite = LambdaMetafactory.metafactory(lookup, "accept", MHBC_INVOKED_TYPE, MHBC_GENERIC_TYPE, handle, handle.type());
            @SuppressWarnings("unchecked") final var consumer = (BiConsumer<C, E>) callsite.getTarget().invokeExact();
            return consumer;
        } catch (Throwable t) {
            throw FormatUtil.rtExc(t, "Failed to build lambda for event handler [] using deep reflection", this);
        }
    }

    public Method getMethod() {
        return method;
    }

    public Class<C> getComponentClass() {
        return componentClass;
    }

    public TypeRef<E> getEventClassType() {
        return eventClassType;
    }

    public EventListenerPriority getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return componentClass.getSimpleName() + "#" + method.getName();
    }

}
