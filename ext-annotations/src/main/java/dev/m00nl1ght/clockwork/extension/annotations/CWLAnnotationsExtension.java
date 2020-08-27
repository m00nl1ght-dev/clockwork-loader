package dev.m00nl1ght.clockwork.extension.annotations;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.plugin.CWLPlugin;
import dev.m00nl1ght.clockwork.core.plugin.CollectClockworkExtensionsEvent;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventListener;
import dev.m00nl1ght.clockwork.events.EventType;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class CWLAnnotationsExtension {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ClockworkCore core;
    private final EventHandlerAnnotationProcessor processor = new EventHandlerAnnotationProcessor();

    private CWLAnnotationsExtension(ClockworkCore core) {
        this.core = core;
        final var cwlPluginComponent = core.getComponentType(CWLPlugin.class, ClockworkCore.class).orElseThrow();
        final var cwlPlugin = cwlPluginComponent.get(core);
        if (cwlPlugin == null) throw FormatUtil.illStateExc("Internal core component missing");
        cwlPlugin.getCollectExtensionsEventType().addListener(CWLAnnotationsExtension.class, CWLAnnotationsExtension::onCollectExtensionsEvent);
    }

    private void onCollectExtensionsEvent(CollectClockworkExtensionsEvent event) {
        event.registerPluginProcessor(EventHandlerAnnotationProcessor.NAME, processor);
    }

    public static void fetchListeners(EventType<?, ?> eventType) {
        final var target = eventType.getTargetType();
        if (target == null) throw new IllegalArgumentException("EventType is not registered");
        fetchListeners(target.getClockworkCore(), List.of(eventType));
    }

    public static void fetchListeners(ClockworkCore core, Iterable<EventType<?, ?>> eventTypes) {
        final var componentType = core.getComponentType(CWLAnnotationsExtension.class, ClockworkCore.class);
        if (componentType.isEmpty()) throw new IllegalStateException("component type does not exist");
        final var ehc = componentType.get().get(core);
        if (ehc == null) throw new IllegalStateException("component missing");
        eventTypes.forEach(e -> fetchListeners(ehc.processor, e));
    }

    public static <E extends Event, T extends ComponentTarget>
    void fetchListeners(EventHandlerAnnotationProcessor processor, EventType<E, T> eventType) {
        final var listenerMap = processor.getCollectedListeners();
        final var listeners = listenerMap.get(eventType.getEventClassType());
        if (listeners != null && !listeners.isEmpty()) {
            for (final var listener : listeners) {
                final var target = listener.getComponentType().getTargetType();
                if (target.isEquivalentTo(eventType.getTargetType())) {
                    @SuppressWarnings("unchecked")
                    final var castedListener = (EventListener<E, ? extends T, ?>) listener;
                    eventType.addListener(castedListener);
                    LOGGER.debug("Added listener: " + listener);
                }
            }
        }
    }

}
