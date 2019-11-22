package dev.m00nl1ght.clockwork.event;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.EventFilter;
import dev.m00nl1ght.clockwork.event.filter.EventFilterFactory;
import dev.m00nl1ght.clockwork.event.filter.CancellableEventFilterFactory;
import dev.m00nl1ght.clockwork.event.filter.GenericEventFilterFactory;
import dev.m00nl1ght.clockwork.processor.PluginProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class EventAnnotationProcessor implements PluginProcessor {

    public static final String NAME = "core.event.annotation";
    private static final Logger LOGGER = LogManager.getLogger();
    private static final MethodType GENERIC_TYPE = MethodType.methodType(Void.TYPE, Object.class, Object.class);
    private static final MethodType INVOKED_TYPE = MethodType.methodType(BiConsumer.class);
    private static final List<EventFilterFactory> filterFactories = new LinkedList<>();

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
                    LOGGER.debug("Registering listener: " + compClass.getSimpleName() + "::" + method.getName() + " to " + params[0].getSimpleName());
                    registerListener(component, params[0], callsite, method);
                } else {
                    LOGGER.error("Invalid event handler [" + component.getComponentClass() + ":" + method.getName() + "] in component [" + component.getId() + "]");
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <C, E, T extends ComponentTarget> void registerListener(ComponentType<C, T> componentType, Class<E> eventClass, CallSite callSite, Method method) throws Throwable {
        final var listener = (BiConsumer<C, E>) callSite.getTarget().invokeExact();

        EventFilter<E, T> filter = null;
        for (var factory : filterFactories) {
            final var ret = factory.get(componentType, eventClass, method);
            if (ret != null) filter = filter == null ? ret : filter.and(ret);
        }

        componentType.getTargetType().getPrimer().registerListener(componentType, eventClass, listener, filter);
    }

    @Override
    public String getName() {
        return NAME;
    }

    public static void registerEventFilterFactory(EventFilterFactory factory) {
        filterFactories.add(factory);
    }

    static {
        registerEventFilterFactory(CancellableEventFilterFactory.INSTANCE);
        registerEventFilterFactory(GenericEventFilterFactory.INSTANCE);
    }

}
