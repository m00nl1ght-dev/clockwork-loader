package dev.m00nl1ght.clockwork.extension.annotations;

import dev.m00nl1ght.clockwork.core.LoadedPlugin;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.event.EventBus;
import dev.m00nl1ght.clockwork.event.EventListener;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader.AccessLevel;
import dev.m00nl1ght.clockwork.loader.LoaderExtension;
import dev.m00nl1ght.clockwork.utils.config.Config;
import dev.m00nl1ght.clockwork.utils.config.MapToSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Set;

public final class CWLAnnotationsExtension extends LoaderExtension {

    private final MapToSet<Class<?>, EventHandlerMethod<?, ?, ?>> collectedHandlers = new MapToSet<>();

    public CWLAnnotationsExtension(@NotNull ClockworkLoader loader) {
        super(loader);
    }

    public static void applyToEventBus(@NotNull ClockworkLoader loader, @NotNull EventBus<Event> eventBus) {
        loader.getExtension(CWLAnnotationsExtension.class).applyToEventBus(eventBus);
    }

    public void applyToEventBus(@NotNull EventBus<Event> eventBus) {
        for (final var handler : collectedHandlers.getAll()) {
            if (!eventBus.addListener(handler)) {
                CWLAnnotations.LOGGER.warn("Failed to add handler to event bus: " + handler);
            }
        }
    }

    @Override
    public void onCoreInitialised() {
        for (final var plugin : target.getCore().getLoadedPlugins()) {
            if (plugin.getDescriptor().getExtData().getOrDefault("usesEventHandlers", Config.BOOLEAN, false)) {
                for (final var component : plugin.getComponentTypes()) {
                    try {
                        collectHandlers(plugin, component.getComponentClass());
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to collect EventHandlers in component [" + component + "]", e);
                    }
                }
            }
        }
    }

    private void collectHandlers(LoadedPlugin plugin, Class<?> handlerClass) {

        // Prepare a var for the potentially needed lookup.
        MethodHandles.Lookup lookup = null;
        final var collected = new ArrayList<EventHandlerMethod<?, ?, ?>>();

        // Build a handler from all methods that have the annotation.
        for (var method : handlerClass.getDeclaredMethods()) {
            final var annotation = method.getAnnotation(EventHandler.class);
            if (annotation != null) {
                final var priority = annotation == null ? EventListener.Phase.NORMAL : annotation.value();
                if (lookup == null) lookup = target.getReflectiveAccess(plugin, handlerClass, AccessLevel.FULL);
                final var handler = EventHandlerMethod.build(target.getCore(), method, lookup, priority);
                if (handler != null) {
                    collected.add(handler);
                } else {
                    CWLAnnotations.LOGGER.error("Invalid event handler: ", handlerClass + "#" + method.getName());
                }
            }
        }

        // Add collected handlers to the set all at once.
        collectedHandlers.put(handlerClass, Set.copyOf(collected));

    }

    public @Nullable MapToSet<Class<?>, EventHandlerMethod<?, ?, ?>> getCollectedHandlers() {
        return collectedHandlers;
    }

}
