package dev.m00nl1ght.clockwork.extension.annotations;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ClockworkExtension;
import dev.m00nl1ght.clockwork.core.ExtensionContext;
import dev.m00nl1ght.clockwork.events.impl.EventBusImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class CWLAnnotationsExtension implements ClockworkExtension {

    static final Logger LOGGER = LogManager.getLogger("Clockwork-Ext-Annotations");

    private final ClockworkCore core;
    private EventHandlers collectedHandlers;

    public CWLAnnotationsExtension(@NotNull ClockworkCore core) {
        this.core = Objects.requireNonNull(core);
    }

    public static void applyToEventBus(@NotNull EventBusImpl eventBus) {
        for (final var handler : getInstance(eventBus.getCore()).collectedHandlers.getAll()) {
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
