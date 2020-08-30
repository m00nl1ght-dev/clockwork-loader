package dev.m00nl1ght.clockwork.extension.annotations;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.plugin.CWLPlugin;
import dev.m00nl1ght.clockwork.core.plugin.CollectClockworkExtensionsEvent;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventType;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public final class CWLAnnotationsExtension {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ClockworkCore core;

    private EventHandlerRegistry collectedHandlers;

    private CWLAnnotationsExtension(ClockworkCore core) {
        this.core = core;
        this.attachEventListener();
    }

    public static void buildListeners(EventType<?, ?> eventType) {
        final var target = eventType.getTargetType();
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
        final var core = eventType.getTargetType().getClockworkCore();
        final var component = core.getComponentType(method.getComponentClass());
        if (component.isPresent()) {
            @SuppressWarnings("unchecked")
            final var comp = (ComponentType<C, ? extends T>) component.get();
            if (comp.getTargetType().isEquivalentTo(eventType.getTargetType())) {
                eventType.addListener(method.buildListener(comp));
            }
        }
    }

    private void attachEventListener() {
        final var cwlPluginComponent = core.getComponentType(CWLPlugin.class, ClockworkCore.class).orElseThrow();
        final var cwlPlugin = cwlPluginComponent.get(core);
        if (cwlPlugin == null) throw FormatUtil.illStateExc("Internal core component missing");
        cwlPlugin.getCollectExtensionsEventType().addListener(CWLAnnotationsExtension.class,
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
