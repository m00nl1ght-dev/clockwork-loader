package dev.m00nl1ght.clockwork.extension.annotations;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ClockworkLoader;
import dev.m00nl1ght.clockwork.core.PluginProcessor;
import dev.m00nl1ght.clockwork.core.PluginProcessorContext;
import dev.m00nl1ght.clockwork.core.plugin.CollectClockworkExtensionsEvent;
import dev.m00nl1ght.clockwork.events.EventListenerPriority;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.LinkedHashSet;

public final class EventHandlerAnnotationProcessor implements PluginProcessor {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String NAME = "extension.annotations.eventhandler";

    private EventHandlerRegistry.Builder registryBuilder;
    private EventHandlerRegistry inherited;

    private EventHandlerAnnotationProcessor() {}

    public static void registerTo(ClockworkLoader loader) {
        loader.registerPluginProcessor(NAME, new EventHandlerAnnotationProcessor());
    }

    public static void registerTo(CollectClockworkExtensionsEvent event) {
        event.registerPluginProcessor(NAME, new EventHandlerAnnotationProcessor());
    }

    @Override
    public void onLoadingStart(ClockworkCore core, ClockworkCore parentCore) {
        if (registryBuilder != null) throw new IllegalStateException();
        this.registryBuilder = EventHandlerRegistry.builder();
        if (parentCore != null) {
            final var inheritingFrom = getExtension(parentCore);
            if (inheritingFrom != null) {
                this.inherited = inheritingFrom.getCollectedHandlers();
            }
        }
    }

    @Override
    public void process(PluginProcessorContext context) {
        if (registryBuilder == null) throw new IllegalStateException();
        for (final var component : context.getPlugin().getComponentTypes()) {
            try {
                process(context, component.getComponentClass());
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access component [" + component + "] using deep reflection", e);
            }
        }
    }

    private <C> void process(PluginProcessorContext context, Class<C> handlerClass) throws IllegalAccessException {

        // If the parent has the handlers for this class, there is no need to build them again.
        if (inherited != null) {
            final var handlers = inherited.getForHandlerClass(handlerClass);
            if (handlers != null) {
                this.registryBuilder.put(handlerClass, handlers);
                return;
            }
        }

        // Prepare a var for the potentially needed lookup and collection.
        MethodHandles.Lookup lookup = null;
        final var collected = new LinkedHashSet<EventHandlerMethod<?, C>>();

        // Build a handler from all methods that have the annotation.
        for (var method : handlerClass.getDeclaredMethods()) {
            final var annotation = method.getAnnotation(EventHandler.class);
            if (annotation != null) {
                final var priority = annotation == null ? EventListenerPriority.NORMAL : annotation.value();
                if (lookup == null) lookup = context.getReflectiveAccess(handlerClass);
                final var handler = EventHandlerMethod.build(lookup, handlerClass, method, priority);
                if (handler != null) {
                    collected.add(handler);
                } else {
                    final var name = handlerClass + "#" + method.getName();
                    LOGGER.error("Invalid event handler [" + name + "]");
                }
            }
        }

        // Add the handlers to the builder.
        this.registryBuilder.put(handlerClass, collected);

    }

    @Override
    public void onLoadingComplete(ClockworkCore core) {
        if (registryBuilder == null) throw new IllegalStateException();
        final var extension = getExtension(core);
        if (extension != null) extension.setCollectedHandlers(registryBuilder.build());
        this.registryBuilder = null;
        this.inherited = null;
    }

    private CWLAnnotationsExtension getExtension(ClockworkCore core) {
        final var componentType = core.getComponentType(CWLAnnotationsExtension.class, ClockworkCore.class).orElseThrow();
        return componentType.get(core);
    }

}
