package dev.m00nl1ght.clockwork.extension.annotations;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.LoadedPlugin;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventListener;
import dev.m00nl1ght.clockwork.events.EventListenerPriority;
import dev.m00nl1ght.clockwork.processor.PluginProcessor;
import dev.m00nl1ght.clockwork.processor.ReflectiveAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;

public class EventHandlerAnnotationProcessor implements PluginProcessor {

    public static final String NAME = "eventhandler-annotation-processor";

    private static final Logger LOGGER = LogManager.getLogger();

    private static final MethodType GENERIC_TYPE = MethodType.methodType(Void.TYPE, Object.class, Object.class);
    private static final MethodType INVOKED_TYPE = MethodType.methodType(BiConsumer.class);

    @Override
    public void process(LoadedPlugin plugin, ReflectiveAccess reflectiveAccess) {
        for (final var component : plugin.getComponentTypes()) {
            final var compClass = component.getComponentClass();
            final var methods = compClass.getDeclaredMethods();
            MethodHandles.Lookup lookup = null;
            for (var method : methods) {
                final var annotation = method.getAnnotation(EventHandler.class);
                if (annotation != null) {
                    final var params = method.getParameterTypes();
                    final var methodStr = component.getComponentClass() + "#" + method.getName();
                    final var priority = annotation == null ? EventListenerPriority.NORMAL : annotation.value();
                    if (params.length == 1 && Event.class.isAssignableFrom(params[0]) && !Modifier.isStatic(method.getModifiers())) {
                        try {
                            if (lookup == null) lookup = reflectiveAccess.lookup(compClass);
                            final var handle = lookup.unreflect(method);
                            final var callsite = LambdaMetafactory.metafactory(lookup, "accept", INVOKED_TYPE, GENERIC_TYPE, handle, handle.type());
                            LOGGER.debug("Registering listener [" + methodStr + "] to [" + params[0].getSimpleName() + "]");
                            @SuppressWarnings("unchecked") final var eventClass = (Class<? extends Event>) params[0];
                            registerListener(component, eventClass, priority, callsite);
                        } catch (Throwable t) {
                            throw new RuntimeException("Failed to build event handler [" + methodStr + "] in component [" + component + "] using deep reflection", t);
                        }
                    } else {
                        LOGGER.error("Invalid event handler [" + methodStr + "] in component [" + component + "]");
                    }
                }
            }
        }
    }

    private <E extends Event, C, T extends ComponentTarget> void
    registerListener(ComponentType<C, T> componentType, Class<E> eventClass, EventListenerPriority priority, CallSite callSite) throws Throwable {
        @SuppressWarnings("unchecked") final var consumer = (BiConsumer<C, E>) callSite.getTarget().invokeExact();
        final var listener = new EventListener<>(eventClass, componentType, priority, consumer);
        // TODO
    }

}
