package dev.m00nl1ght.clockwork.event;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.EventFilter;
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

@SuppressWarnings("unchecked")
public class EventAnnotationProcessor implements PluginProcessor {

    public static final String NAME = "core.event.annotation";
    private static final Logger LOGGER = LogManager.getLogger();
    private static final MethodType GENERIC_TYPE = MethodType.methodType(Void.TYPE, Object.class, Object.class);
    private static final MethodType INVOKED_TYPE = MethodType.methodType(BiConsumer.class);

    @Override
    public <C, T extends ComponentTarget<? super T>> void process(ComponentType<C, T> component, Supplier<MethodHandles.Lookup> reflectiveAccess) throws Throwable {
        final var compClass = component.getComponentClass();
        final var methods = compClass.getDeclaredMethods();
        for (var method : methods) {
            if (method.isAnnotationPresent(EventHandler.class)) {
                final var params = method.getParameterTypes();
                if (params.length == 1 && Event.class.isAssignableFrom(params[0]) && !Modifier.isStatic(method.getModifiers())) {
                    final var lookup = reflectiveAccess.get();
                    final var handle = lookup.unreflect(method);
                    final var callsite = LambdaMetafactory.metafactory(lookup, "accept", INVOKED_TYPE, GENERIC_TYPE, handle, handle.type());
                    LOGGER.debug("Registering listener: " + compClass.getSimpleName() + "::" + method.getName() + " to " + params[0].getSimpleName());
                    registerListener(component, params[0], callsite);
                } else {
                    LOGGER.error("Invalid event handler [" + component.getComponentClass() + ":" + method.getName() + "] in component [" + component.getId() + "]");
                }
            }
        }
    }

    private <C, T extends ComponentTarget<? super T>, E> void registerListener(ComponentType<C, T> componentType, Class<E> eventClass, CallSite callSite) throws Throwable {
        final var listener = (BiConsumer<C, E>) callSite.getTarget().invokeExact();
        final var filter = filterFor(componentType, eventClass);
        componentType.getTargetType().getPrimer().registerListener(componentType, eventClass, listener, filter);
    }

    private <C, E, T extends ComponentTarget<? super T>> EventFilter<E, T> filterFor(ComponentType<C, T> componentType, Class<E> eventClass) {
        return null; // TODO
    }

    @Override
    public String getName() {
        return NAME;
    }

}
