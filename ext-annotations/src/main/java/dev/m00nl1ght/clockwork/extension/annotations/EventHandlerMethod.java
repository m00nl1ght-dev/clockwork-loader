package dev.m00nl1ght.clockwork.extension.annotations;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.listener.EventListenerPriority;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.events.listener.SimpleEventListener;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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

    public static EventHandlerMethod<?, ?> build(MethodHandles.Lookup lookup, Class<?> handlerClass, Method method, EventListenerPriority priority) {
        if (method.getDeclaringClass() != handlerClass) throw new IllegalArgumentException();
        final var params = method.getGenericParameterTypes();
        if (Modifier.isStatic(method.getModifiers())) {
            if (params.length != 2) return null;
            final var eventType = eventTypeRef(params[1]);
            if (!(params[0] instanceof Class) || eventType == null) return null;
            return new EventHandlerMethod<>(lookup, method, (Class<?>) params[0], eventType, priority);
        } else {
            if (params.length != 1) return null;
            final var eventType = eventTypeRef(params[0]);
            if (eventType == null) return null;
            return new EventHandlerMethod<>(lookup, method, handlerClass, eventType, priority);
        }
    }

    private static TypeRef<? extends Event> eventTypeRef(Type paramType) {
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

    private EventHandlerMethod(MethodHandles.Lookup lookup, Method method, Class<C> componentClass, TypeRef<E> eventClassType, EventListenerPriority priority) {
        this.lookup = lookup;
        this.method = method;
        this.componentClass = componentClass;
        this.eventClassType = eventClassType;
        this.priority = priority;
    }

    public <T extends ComponentTarget> EventListener<E, T, C> buildListener(ComponentType<C, T> componentType) {
        if (cachedLambda == null) cachedLambda = buildConsumer();
        return new SimpleEventListener<>(eventClassType, componentType, priority, cachedLambda);
    }

    private BiConsumer<C, E> buildConsumer() {
        // TODO this breaks in JVM 1.15+ because of new hidden classes mechanics
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
