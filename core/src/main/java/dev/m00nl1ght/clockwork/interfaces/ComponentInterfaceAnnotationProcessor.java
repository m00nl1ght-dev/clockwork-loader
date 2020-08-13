package dev.m00nl1ght.clockwork.interfaces;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.processor.PluginProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.function.Supplier;

public class ComponentInterfaceAnnotationProcessor implements PluginProcessor {

    public static final String NAME = "core.subtarget.annotation";
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void process(ComponentType<?, ?> component, Supplier<MethodHandles.Lookup> reflectiveAccess) {
        findTypes(component, component.getComponentClass());
    }

    private <C, T extends ComponentTarget> void findTypes(ComponentType<C, T> component, Class<?> theClass) {
        final var sc = theClass.getSuperclass();
        if (sc != null) findTypes(component, sc);
        for (var i : theClass.getInterfaces()) findTypes(component, i);
        if (theClass.isAnnotationPresent(ComponentInterface.class)) {
            LOGGER.debug("Registering Subtarget [" + theClass.getSimpleName() + "] for component [" + component.getId() + "]");
            registerSubtarget(theClass, component);
        }
    }

    private <I, T extends ComponentTarget> void registerSubtarget(Class<I> interfaceClass, ComponentType<?, T> componentType) {
        @SuppressWarnings("unchecked") final var componentCasted = (ComponentType<? extends I, T>) componentType;
        componentType.getTargetType().getPrimer().registerSubtarget(interfaceClass, componentCasted);
    }

    @Override
    public String getName() {
        return NAME;
    }

}
