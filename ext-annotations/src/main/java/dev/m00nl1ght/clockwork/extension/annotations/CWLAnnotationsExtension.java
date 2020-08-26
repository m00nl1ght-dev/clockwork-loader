package dev.m00nl1ght.clockwork.extension.annotations;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CWLAnnotationsExtension {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ClockworkCore core;
    private final EventHandlerAnnotationProcessor processor = new EventHandlerAnnotationProcessor();

    private CWLAnnotationsExtension(ClockworkCore core) {
        this.core = core;
    }

    public static void fetchListeners(EventType<?, ?> eventType) {
        final var target = eventType.getTargetType();
        if (target == null) throw new IllegalArgumentException("EventType is not registered");
        final var core = target.getClockworkCore();
        final var componentType = core.getComponentType(CWLAnnotationsExtension.class, ClockworkCore.class);
        if (componentType.isEmpty()) throw new IllegalStateException("component type does not exist");
        final var ehc = componentType.get().get(core);
        if (ehc == null) throw new IllegalStateException("component missing");
        fetchListeners(ehc.processor, eventType);
    }

    public static <E extends Event, T extends ComponentTarget>
    void fetchListeners(EventHandlerAnnotationProcessor processor, EventType<E, T> eventType) {
        final var listenerMap = processor.getCollectedListeners();
        final var listeners = listenerMap.get(eventType.getEventClassType());
        if (listeners != null && !listeners.isEmpty()) {
            for (final var listener : listeners) {
                // TODO
            }
        }
    }

}
