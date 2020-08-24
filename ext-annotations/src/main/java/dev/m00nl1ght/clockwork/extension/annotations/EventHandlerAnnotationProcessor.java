package dev.m00nl1ght.clockwork.extension.annotations;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventListener;
import dev.m00nl1ght.clockwork.events.EventListenerPriority;
import dev.m00nl1ght.clockwork.processor.PluginProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class EventHandlerAnnotationProcessor implements PluginProcessor {

    public static final String NAME = "eventhandler-annotation-processor";

    private static final Logger LOGGER = LogManager.getLogger();

    private static final MethodType GENERIC_TYPE = MethodType.methodType(Void.TYPE, Object.class, Object.class);
    private static final MethodType INVOKED_TYPE = MethodType.methodType(BiConsumer.class);

    @Override
    public void process(ComponentType<?, ?> component, Supplier<MethodHandles.Lookup> reflectiveAccess) throws Throwable {
        final var compClass = component.getComponentClass();
        final var methods = compClass.getDeclaredMethods();
        for (var method : methods) {
            if (method.isAnnotationPresent(EventHandler.class)) {
                final var params = method.getParameterTypes();
                if (params.length == 1 && Event.class.isAssignableFrom(params[0]) && !Modifier.isStatic(method.getModifiers())) {
                    final var lookup = reflectiveAccess.get();
                    final var handle = lookup.unreflect(method);
                    final var callsite = LambdaMetafactory.metafactory(lookup, "accept", INVOKED_TYPE, GENERIC_TYPE, handle, handle.type());
                    LOGGER.debug("Registering listener [" + compClass.getSimpleName() + "::" + method.getName() + "] to [" + params[0].getSimpleName() + "]");
                    @SuppressWarnings("unchecked") final var eventClass = (Class<? extends Event>) params[0];
                    registerListener(component, eventClass, callsite, method);
                } else {
                    LOGGER.error("Invalid event handler [" + component.getComponentClass() + ":" + method.getName() + "] in component [" + component.getId() + "]");
                }
            }
        }
    }

    private <E extends Event, C, T extends ComponentTarget> void
    registerListener(ComponentType<C, T> componentType, Class<E> eventClass, CallSite callSite, Method method) throws Throwable {
        @SuppressWarnings("unchecked") final var consumer = (BiConsumer<C, E>) callSite.getTarget().invokeExact();
        final var annotation = method.getAnnotation(EventHandler.class);
        final var priority = annotation == null ? EventListenerPriority.NORMAL : annotation.value();
        final var listener = new EventListener<>(eventClass, componentType, priority, consumer);
        // TODO
    }

    @Override
    public String getName() {
        return NAME;
    }

}
