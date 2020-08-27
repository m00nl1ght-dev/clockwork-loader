package dev.m00nl1ght.clockwork.extension.annotations.eventhandler;

import dev.m00nl1ght.clockwork.core.PluginProcessor;
import dev.m00nl1ght.clockwork.core.PluginProcessorContext;
import dev.m00nl1ght.clockwork.events.EventListenerPriority;
import dev.m00nl1ght.clockwork.util.TypeRef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.*;

public class EventHandlerAnnotationProcessor implements PluginProcessor {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String NAME = "extension.annotations.eventhandler";

    private final Map<TypeRef<?>, Set<EventHandlerMethod<?, ?>>> collectedMethods = new HashMap<>();

    @Override
    public void process(PluginProcessorContext pluginProcessorContext) {
        final var plugin = pluginProcessorContext.getPlugin();
        for (final var component : plugin.getComponentTypes()) {
            try {
                MethodHandles.Lookup lookup = null;
                for (var method : component.getComponentClass().getDeclaredMethods()) {
                    final var annotation = method.getAnnotation(EventHandler.class);
                    if (annotation != null) {
                        final var priority = annotation == null ? EventListenerPriority.NORMAL : annotation.value();
                        if (lookup == null) lookup = pluginProcessorContext.getReflectiveAccess(component.getComponentClass());
                        final var handler = EventHandlerMethod.build(lookup, method, priority);
                        if (handler != null) {
                            final var evt = handler.getEventClassType();
                            final var handlers = collectedMethods.computeIfAbsent(evt, e -> new LinkedHashSet<>());
                            handlers.add(handler);
                        } else {
                            final var name = component.getComponentClass() + "#" + method.getName();
                            LOGGER.error("Invalid event handler [" + name + "]");
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access component [" + component + "] using deep reflection", e);
            }
        }
    }

    public Set<EventHandlerMethod<?, ?>> getCollectedMethods(TypeRef<?> eventClassType) {
        return Collections.unmodifiableSet(collectedMethods.getOrDefault(eventClassType, Collections.emptySet()));
    }

    public void inheritMethodsFrom(EventHandlerAnnotationProcessor other) {
        for (final var evt : other.collectedMethods.entrySet()) {
            final var list = this.collectedMethods.computeIfAbsent(evt.getKey(), e -> new LinkedHashSet<>());
            list.addAll(evt.getValue());
        }
    }

}
