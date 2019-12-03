package dev.m00nl1ght.clockwork.subtarget;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.processor.PluginProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.function.Supplier;

public class SubtargetAnnotationProcessor implements PluginProcessor {

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
        if (theClass.isAnnotationPresent(SubtargetType.class)) {
            LOGGER.debug("Registering Subtarget [" + theClass.getSimpleName() + "] for component [" + component.getId() + "]");
            component.getTargetType().getPrimer().registerSubtarget(component, theClass);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

}
