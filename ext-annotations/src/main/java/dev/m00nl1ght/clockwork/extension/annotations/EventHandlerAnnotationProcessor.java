package dev.m00nl1ght.clockwork.extension.annotations;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.PluginProcessor;
import dev.m00nl1ght.clockwork.core.PluginProcessorContext;
import dev.m00nl1ght.clockwork.core.PluginProcessorContext.AccessLevel;
import dev.m00nl1ght.clockwork.event.EventListener;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.util.*;

public final class EventHandlerAnnotationProcessor implements PluginProcessor {

    public static final String NAME = "extension.annotations.eventhandler";

    private EventHandlers inherited;
    private Map<Class<?>, Set<EventHandlerMethod<?, ?, ?>>> handlers;

    private EventHandlerAnnotationProcessor() {}

    public static void registerTo(@NotNull Registry<PluginProcessor> registry) {
        Objects.requireNonNull(registry).register(NAME, new EventHandlerAnnotationProcessor());
    }

    @Override
    public void onLoadingStart(@NotNull ClockworkCore core, @Nullable ClockworkCore parentCore) {
        if (handlers != null) throw new IllegalStateException();
        this.handlers = new HashMap<>();
        if (parentCore != null) {
            final var inheritingFrom = getExtension(parentCore);
            if (inheritingFrom != null) {
                this.inherited = inheritingFrom.getCollectedHandlers();
            }
        }
    }

    @Override
    public void processLate(@NotNull PluginProcessorContext context) {
        if (handlers == null) throw new IllegalStateException();
        for (final var component : context.getPlugin().getComponentTypes()) {
            try {
                process(context, component.getComponentClass());
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access component [" + component + "] using deep reflection", e);
            }
        }
    }

    private void process(PluginProcessorContext context, Class<?> handlerClass) throws IllegalAccessException {

        // If the parent has the handlers for this class, there is no need to build them again.
        if (inherited != null) {
            final var found = inherited.get(handlerClass);
            if (found != null) {
                this.handlers.put(handlerClass, found);
                return;
            }
        }

        // Prepare a var for the potentially needed lookup.
        MethodHandles.Lookup lookup = null;
        final var collected = new ArrayList<EventHandlerMethod<?, ?, ?>>();

        // Build a handler from all methods that have the annotation.
        for (var method : handlerClass.getDeclaredMethods()) {
            final var annotation = method.getAnnotation(EventHandler.class);
            if (annotation != null) {
                final var priority = annotation == null ? EventListener.Phase.NORMAL : annotation.value();
                if (lookup == null) lookup = context.getReflectiveAccess(handlerClass, AccessLevel.FULL);
                final var handler = EventHandlerMethod.build(context.getPlugin().getClockworkCore(), method, lookup, priority);
                if (handler != null) {
                    collected.add(handler);
                } else {
                    CWLAnnotationsExtension.LOGGER.error("Invalid event handler {}#{}", handlerClass, method.getName());
                }
            }
        }

        handlers.put(handlerClass, Set.copyOf(collected));

    }

    @Override
    public void onLoadingComplete(@NotNull ClockworkCore core) {
        if (handlers == null) throw new IllegalStateException();
        final var extension = getExtension(core);
        if (extension != null) extension.setCollectedHandlers(new EventHandlers(handlers));
        this.handlers = null;
        this.inherited = null;
    }

    private CWLAnnotationsExtension getExtension(ClockworkCore core) {
        final var componentType = core.getComponentType(CWLAnnotationsExtension.class, ClockworkCore.class)
                .orElseThrow(() -> FormatUtil.rtExc("Extension was not loaded correctly, internal component is missing"));
        return componentType.get(core);
    }

}
