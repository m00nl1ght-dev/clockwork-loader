package dev.m00nl1ght.clockwork.extension.annotations;

import dev.m00nl1ght.clockwork.core.*;
import dev.m00nl1ght.clockwork.core.plugin.CWLPlugin;
import dev.m00nl1ght.clockwork.core.plugin.CollectClockworkExtensionsEvent;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventType;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.List;

public final class CWLAnnotationsExtension {

    private static final Logger LOGGER = LogManager.getLogger();
    static final MethodHandles.Lookup LOCAL_LOOKUP = MethodHandles.lookup();

    private final ClockworkCore core;

    private EventHandlerRegistry collectedHandlers;

    private CWLAnnotationsExtension(ClockworkCore core) {
        this.core = core;
        this.attachEventListener();
    }

    public static void buildListeners(EventType<?, ?> eventType) {
        if (!(eventType.getTargetType() instanceof RegisteredTargetType)) return;
        final var target = (RegisteredTargetType) eventType.getTargetType();
        if (target == null) throw new IllegalArgumentException("EventType is not registered");
        buildListeners(target.getClockworkCore(), List.of(eventType));
    }

    public static void buildListeners(ClockworkCore core, Iterable<? extends EventType<?, ?>> eventTypes) {
        final var componentType = core.getComponentType(CWLAnnotationsExtension.class, ClockworkCore.class);
        if (componentType.isEmpty()) throw new IllegalStateException("component type does not exist");
        final var ehc = componentType.get().get(core);
        if (ehc == null) throw new IllegalStateException("component missing");
        eventTypes.forEach(e -> buildListeners(ehc.collectedHandlers, e));
    }

    public static <E extends Event, T extends ComponentTarget>
    void buildListeners(EventHandlerRegistry collectedHandlers, EventType<E, T> eventType) {
        final var methods = collectedHandlers.getForEventType(eventType.getEventClassType());
        if (methods != null) for (final var method : methods) buildListener(eventType, method);
    }

    // ### Internal ###

    private static <E extends Event, T extends ComponentTarget, C>
    void buildListener(EventType<E, T> eventType, EventHandlerMethod<E, C> method) {
        boolean found = false;
        for (final var targetType : eventType.getCompatibleTargetTypes()) {
            final var component = targetType.getComponentType(method.getComponentClass());
            if (component.isPresent()) {
                @SuppressWarnings("unchecked")
                final var comp = (ComponentType<C, ? extends T>) component.get();
                if (comp.getTargetType() == targetType) {
                    eventType.addListener(method.buildListener(comp));
                    found = true;
                }
            }
        }
        if (!found) {
            LOGGER.warn("Event handler {} is not compatible with EventType {}", method, eventType);
        }
    }

    private void attachEventListener() {
        final var cwlPluginComponent = core.getComponentType(CWLPlugin.class, ClockworkCore.class).orElseThrow();
        final var cwlPlugin = cwlPluginComponent.get(core);
        if (cwlPlugin == null) throw FormatUtil.illStateExc("Internal core component missing");
        cwlPlugin.getCollectExtensionsEventType().addListener(core, CWLAnnotationsExtension.class,
                CWLAnnotationsExtension::onCollectExtensionsEvent);
    }

    private void onCollectExtensionsEvent(CollectClockworkExtensionsEvent event) {
        EventHandlerAnnotationProcessor.registerTo(event);
    }

    EventHandlerRegistry getCollectedHandlers() {
        return collectedHandlers;
    }

    void setCollectedHandlers(EventHandlerRegistry collectedHandlers) {
        this.collectedHandlers = collectedHandlers;
    }

}
