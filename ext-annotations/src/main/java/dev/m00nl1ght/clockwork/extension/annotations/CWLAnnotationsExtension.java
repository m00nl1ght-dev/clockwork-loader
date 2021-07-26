package dev.m00nl1ght.clockwork.extension.annotations;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.loader.ClockworkExtension;
import dev.m00nl1ght.clockwork.loader.ExtensionContext;
import dev.m00nl1ght.clockwork.core.MainComponent;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.event.EventBus;
import dev.m00nl1ght.clockwork.logger.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class CWLAnnotationsExtension extends MainComponent implements ClockworkExtension {

    static final Logger LOGGER = Logger.create("Clockwork-Ext-Annotations");

    private EventHandlers collectedHandlers;

    public CWLAnnotationsExtension(@NotNull ClockworkCore core) {
        super(core);
    }

    public static void applyToEventBus(@NotNull ClockworkCore core, @NotNull EventBus<Event> eventBus) {
        for (final var handler : getInstance(core).collectedHandlers.getAll()) {
            if (!eventBus.addListener(handler)) {
                LOGGER.warn("Failed to add handler to event bus: " + handler);
            }
        }
    }

    @Override
    public void registerFeatures(@NotNull ExtensionContext extensionContext) {
        EventHandlerAnnotationProcessor.registerTo(extensionContext.getProcessorRegistry());
    }

    public static @NotNull CWLAnnotationsExtension getInstance(@NotNull ClockworkCore core) {
        Objects.requireNonNull(core).getState().requireOrAfter(ClockworkCore.State.INITIALISED);
        final var componentType = core.getComponentTypeOrThrow(CWLAnnotationsExtension.class, ClockworkCore.class);
        final var ehc = componentType.get(core);
        if (ehc == null) throw new IllegalStateException("component missing");
        return ehc;
    }

    public @Nullable EventHandlers getCollectedHandlers() {
        return collectedHandlers;
    }

    public void setCollectedHandlers(@Nullable EventHandlers collectedHandlers) {
        this.collectedHandlers = collectedHandlers;
    }

}
