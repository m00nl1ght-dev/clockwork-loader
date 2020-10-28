package dev.m00nl1ght.clockwork.extension.annotations;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.events.EventDispatcher;
import dev.m00nl1ght.clockwork.events.NestedEventDispatcher;
import dev.m00nl1ght.clockwork.events.StaticEventDispatcher;
import dev.m00nl1ght.clockwork.events.impl.ContextAwareEvent;
import dev.m00nl1ght.clockwork.events.impl.EventBusImpl;
import dev.m00nl1ght.clockwork.util.FormatUtil;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Objects;

public class ExtEventBusImpl extends EventBusImpl {

    protected EventHandlerRegistry registry;

    public ExtEventBusImpl(ClockworkCore core) {
        super(core);
    }

    public void bind() {
        this.bind(CWLAnnotationsExtension.getInstance(core).getCollectedHandlers());
    }

    public void bind(EventHandlerRegistry registry) {
        if (this.registry != null) throw FormatUtil.illStateExc("already bound");
        this.registry = Objects.requireNonNull(registry);
        getEventDispatchers().forEach(d -> CWLAnnotationsExtension.buildListeners(registry, d));
    }

    @Override
    public <E extends ContextAwareEvent, T extends ComponentTarget>
    EventDispatcher<E, T> getEventDispatcher(TypeRef<E> eventType, Class<T> targetClass) {
        final var dispatcher = super.getEventDispatcher(eventType, targetClass);
        if (registry != null) CWLAnnotationsExtension.buildListeners(registry, dispatcher);
        return dispatcher;
    }

    @Override
    public <E extends ContextAwareEvent, O extends ComponentTarget, T extends ComponentTarget>
    NestedEventDispatcher<E, T, O> getNestedEventDispatcher(TypeRef<E> eventType, Class<T> targetClass, Class<O> originClass) {
        final var dispatcher = super.getNestedEventDispatcher(eventType, targetClass, originClass);
        if (registry != null) CWLAnnotationsExtension.buildListeners(registry, dispatcher);
        return dispatcher;
    }

    @Override
    public <E extends ContextAwareEvent, O extends ComponentTarget, T extends ComponentTarget> StaticEventDispatcher<E, T, O> getStaticEventDispatcher(TypeRef<E> eventType, Class<T> targetClass, Class<O> originClass, T target) {
        final var dispatcher = super.getStaticEventDispatcher(eventType, targetClass, originClass, target);
        if (registry != null) CWLAnnotationsExtension.buildListeners(registry, dispatcher);
        return dispatcher;
    }

}
