package dev.m00nl1ght.clockwork.extension.eventhandler;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventListener;
import dev.m00nl1ght.clockwork.events.EventType;
import dev.m00nl1ght.clockwork.util.TypeRef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CWLEventHandlerExtension {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ClockworkCore core;
    private final Map<TypeRef<?>, List<?>> collectedListeners = new HashMap<>();

    private CWLEventHandlerExtension(ClockworkCore core) {
        this.core = core;
    }

    public static <E extends Event, T extends ComponentTarget>
    void fetchListeners(EventType<E, T> eventType) {
        final var target = eventType.getTargetType();
        if (target == null) throw new IllegalArgumentException("EventType is not registered");
        final var core = target.getPlugin().getClockworkCore();
        final var componentType = core.getComponentType(CWLEventHandlerExtension.class, ClockworkCore.class);
        if (componentType.isEmpty()) throw new IllegalStateException("component type does not exist");
        final var ehc = componentType.get().get(core);
        if (ehc == null) throw new IllegalStateException("component missing");
        final var listeners = ehc.collectedListeners.remove(eventType.getEventClassType());
        @SuppressWarnings("unchecked") final var listenersCasted = (List<EventListener<E, T, ?>>) listeners;
        if (listeners != null && !listeners.isEmpty()) {
            eventType.addListeners(listenersCasted);
        }
    }

}
