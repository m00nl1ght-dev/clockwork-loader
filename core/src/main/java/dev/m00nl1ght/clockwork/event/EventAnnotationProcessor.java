package dev.m00nl1ght.clockwork.event;

import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.processor.PluginProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class EventAnnotationProcessor implements PluginProcessor {

    public static final String NAME = "core.event.annotation";
    private static final Logger LOGGER = LogManager.getLogger();
    private static final MethodType GENERIC_TYPE = MethodType.methodType(Void.TYPE, Object.class, Object.class);
    private static final MethodType INVOKED_TYPE = MethodType.methodType(BiConsumer.class);

    @Override
    public <C, T> void process(ComponentType<C, T> component, Supplier<MethodHandles.Lookup> reflectiveAccess) throws Throwable {
        final var compClass = component.getComponentClass();
        final var methods = compClass.getDeclaredMethods();
        for (var method : methods) {
            if (method.isAnnotationPresent(EventHandler.class)) {
                final var params = method.getParameterTypes();
                if (params.length == 1 && Event.class.isAssignableFrom(params[0]) && !Modifier.isStatic(method.getModifiers())) {
                    final var lookup = reflectiveAccess.get();
                    final var evtType = component.getTargetType().getEventType(params[0]);
                    final var handle = lookup.unreflect(method);
                    final var callsite = LambdaMetafactory.metafactory(lookup, "accept", INVOKED_TYPE, GENERIC_TYPE, handle, handle.type());
                    LOGGER.debug("Registering listener: " + compClass.getSimpleName() + "::" + method.getName() + " to " + evtType.getEventClass().getSimpleName());
                    registerListener(component, evtType, callsite);
                } else {
                    LOGGER.error("Invalid event handler [" + component.getComponentClass() + ":" + method.getName() + "] in component [" + component.getId() + "]");
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <C, T, E> void registerListener(ComponentType<C, T> componentType, EventType<E, T> eventType, CallSite callSite) throws Throwable {
        final var listener = (BiConsumer<C, E>) callSite.getTarget().invokeExact();
        eventType.registerListener(componentType, listener);
    }

    @Override
    public String getName() {
        return NAME;
    }

}
