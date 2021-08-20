package dev.m00nl1ght.clockwork.extension.annotations;

import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.event.EventBus;
import dev.m00nl1ght.clockwork.loader.ClockworkLoader;
import dev.m00nl1ght.clockwork.loader.LoaderExtension;
import dev.m00nl1ght.clockwork.logger.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CWLAnnotationsExtension extends LoaderExtension {

    static final Logger LOGGER = Logger.create("Clockwork-Ext-Annotations");

    private EventHandlers collectedHandlers;

    public CWLAnnotationsExtension(@NotNull ClockworkLoader loader) {
        super(loader);
        // EventHandlerAnnotationProcessor.registerTo(extensionContext); // TODO
    }

    public static void applyToEventBus(@NotNull ClockworkLoader loader, @NotNull EventBus<Event> eventBus) {
        for (final var handler : loader.getExtension(CWLAnnotationsExtension.class).collectedHandlers.getAll()) {
            if (!eventBus.addListener(handler)) {
                LOGGER.warn("Failed to add handler to event bus: " + handler);
            }
        }
    }

    public @Nullable EventHandlers getCollectedHandlers() {
        return collectedHandlers;
    }

    public void setCollectedHandlers(@Nullable EventHandlers collectedHandlers) {
        this.collectedHandlers = collectedHandlers;
    }

}
