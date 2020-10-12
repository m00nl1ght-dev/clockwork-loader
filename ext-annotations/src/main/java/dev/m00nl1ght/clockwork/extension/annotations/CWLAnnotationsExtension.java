package dev.m00nl1ght.clockwork.extension.annotations;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.core.RegisteredTargetType;
import dev.m00nl1ght.clockwork.core.plugin.CWLPlugin;
import dev.m00nl1ght.clockwork.core.plugin.CollectClockworkExtensionsEvent;
import dev.m00nl1ght.clockwork.events.AbstractEventBus;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventDispatcher;
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

    public static void buildListeners(AbstractEventBus<?> eventBus) {
        buildListeners(eventBus.getCore(), eventBus.getEventDispatchers());
    }

    public static void buildListeners(EventDispatcher<?, ?> eventDispatcher) {
        if (!(eventDispatcher.getTargetType() instanceof RegisteredTargetType)) return;
        final var target = (RegisteredTargetType) eventDispatcher.getTargetType();
        if (target == null) throw new IllegalArgumentException("EventType is not registered");
        buildListeners(target.getClockworkCore(), List.of(eventDispatcher));
    }

    public static void buildListeners(ClockworkCore core, Iterable<? extends EventDispatcher<?, ?>> eventTypes) {
        final var ehc = getInstance(core);
        eventTypes.forEach(e -> buildListeners(ehc.collectedHandlers, e));
    }

    public static <E extends Event, T extends ComponentTarget>
    void buildListeners(EventHandlerRegistry collectedHandlers, EventDispatcher<E, T> eventDispatcher) {
        final var methods = collectedHandlers.getForEventType(eventDispatcher.getEventClassType());
        if (methods != null) for (final var method : methods) buildListener(eventDispatcher, method);
    }

    // ### Internal ###

    private static <E extends Event, T extends ComponentTarget, C>
    void buildListener(EventDispatcher<E, T> eventDispatcher, EventHandlerMethod<E, C> method) {
        boolean found = false;
        for (final var targetType : eventDispatcher.getCompatibleTargetTypes()) {
            final var component = targetType.getComponentType(method.getComponentClass());
            if (component.isPresent()) {
                @SuppressWarnings("unchecked")
                final var comp = (ComponentType<C, ? extends T>) component.get();
                if (comp.getTargetType() == targetType) {
                    eventDispatcher.addListener(method.buildListener(comp));
                    found = true;
                }
            }
        }
        if (!found) {
            LOGGER.warn("Event handler {} is not compatible with EventType {}", method, eventDispatcher);
        }
    }

    public static CWLAnnotationsExtension getInstance(ClockworkCore core) {
        core.getState().requireOrAfter(ClockworkCore.State.INITIALISED);
        final var componentType = core.getComponentType(CWLAnnotationsExtension.class, ClockworkCore.class);
        if (componentType.isEmpty()) throw new IllegalStateException("component type does not exist");
        final var ehc = componentType.get().get(core);
        if (ehc == null) throw new IllegalStateException("component missing");
        return ehc;
    }

    private void attachEventListener() {
        final var cwlPluginComponent = core.getComponentType(CWLPlugin.class, ClockworkCore.class).orElseThrow();
        final var extComponent = core.getComponentType(CWLAnnotationsExtension.class, ClockworkCore.class).orElseThrow();
        final var cwlPlugin = cwlPluginComponent.get(core);
        if (cwlPlugin == null) throw FormatUtil.illStateExc("Internal core component missing");
        cwlPlugin.getCollectExtensionsEventType()
                .addListener(extComponent, CWLAnnotationsExtension::onCollectExtensionsEvent);
    }

    private void onCollectExtensionsEvent(CollectClockworkExtensionsEvent event) {
        EventHandlerAnnotationProcessor.registerTo(event.getProcessorRegistry());
    }

    EventHandlerRegistry getCollectedHandlers() {
        return collectedHandlers;
    }

    void setCollectedHandlers(EventHandlerRegistry collectedHandlers) {
        this.collectedHandlers = collectedHandlers;
    }

}
