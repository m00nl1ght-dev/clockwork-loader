package dev.m00nl1ght.clockwork.event.impl.forwarding;

import dev.m00nl1ght.clockwork.component.ComponentTarget;
import dev.m00nl1ght.clockwork.component.ComponentType;
import dev.m00nl1ght.clockwork.component.TargetType;
import dev.m00nl1ght.clockwork.event.*;
import dev.m00nl1ght.clockwork.event.impl.listener.EventListenerForwardingByComponent;
import dev.m00nl1ght.clockwork.utils.reflect.TypeRef;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

public class EventForwardingPolicyByComponent<B extends Event, S extends ComponentTarget, D extends ComponentTarget> implements EventForwardingPolicy<B, S, D> {

    private final TargetType<D> destinationTarget;
    private final ComponentType<D, S> linkingComponent;
    private final Predicate<TypeRef<? extends B>> eventTypeFilter;
    private final EventBus<B> eventBus;

    private final Observer observer = new Observer();

    public EventForwardingPolicyByComponent(@NotNull TargetType<D> destinationTarget,
                                            @NotNull ComponentType<D, S> linkingComponent,
                                            @NotNull Predicate<TypeRef<? extends B>> eventTypeFilter,
                                            @NotNull EventBus<B> eventBus) {

        this.destinationTarget = Objects.requireNonNull(destinationTarget);
        this.linkingComponent = Objects.requireNonNull(linkingComponent);
        this.eventTypeFilter = Objects.requireNonNull(eventTypeFilter);
        this.eventBus = Objects.requireNonNull(eventBus);
    }

    @Override
    public @NotNull TargetType<S> getSourceTargetType() {
        return linkingComponent.getTargetType();
    }

    @Override
    public @NotNull TargetType<D> getDestinationTargetType() {
        return destinationTarget;
    }

    @Override
    public @NotNull EventBus<B> getEventBus() {
        return eventBus;
    }

    public @NotNull ComponentType<D, S> getLinkingComponent() {
        return linkingComponent;
    }

    @Override
    public <E extends B> void bind(@NotNull EventListenerCollection<E, ?> listeners) {
        if (!eventTypeFilter.test(listeners.getEventType())) return;
        if (destinationTarget.isEquivalentTo(listeners.getTargetType())) {
            listeners.addObserver(observer, true);
        }
    }

    @Override
    public <E extends B> void unbind(@NotNull EventListenerCollection<E, ?> listeners) {
        if (!eventTypeFilter.test(listeners.getEventType())) return;
        if (destinationTarget.isEquivalentTo(listeners.getTargetType())) {
            listeners.removeObserver(observer, true);
        }
    }

    private class Observer implements EventListenerCollection.Observer<B> {

        @Override
        public <E extends B, T extends ComponentTarget, C>
        void onAdded(EventListenerCollection<E, T> collection, EventListener<E, T, C> listener) {
            @SuppressWarnings("unchecked")
            final var castedListener = (EventListener<E, ? super D, C>) listener;
            final var source = eventBus.getListenerCollection(listener.getEventType(), linkingComponent.getTargetType());
            source.add(new EventListenerForwardingByComponent<>(castedListener, linkingComponent));
        }

        @Override
        public <E extends B, T extends ComponentTarget, C>
        void onRemoved(EventListenerCollection<E, T> collection, EventListener<E, T, C> listener) {
            @SuppressWarnings("unchecked")
            final var castedListener = (EventListener<E, ? super D, C>) listener;
            final var source = eventBus.getListenerCollection(listener.getEventType(), linkingComponent.getTargetType());
            source.remove(new EventListenerForwardingByComponent<>(castedListener, linkingComponent));
        }

    }

}
