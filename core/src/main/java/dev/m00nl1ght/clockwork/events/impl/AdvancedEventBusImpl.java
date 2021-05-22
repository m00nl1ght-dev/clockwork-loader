package dev.m00nl1ght.clockwork.events.impl;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.LinkingComponentType;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.EventDispatcher;
import dev.m00nl1ght.clockwork.events.EventListenerCollection;
import dev.m00nl1ght.clockwork.util.TypeRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdvancedEventBusImpl extends EventBusImpl {

    protected final TargetGraph targetGraph = new TargetGraph();

    @Override
    protected <E extends Event, T extends ComponentTarget>
    @NotNull EventDispatcher<E, T> buildDispatcher(@NotNull EventTargetKey<E, T> key) {
        final var dispatcher = EventDispatcher.of(key.getEventType(), key.getTargetType());
        if (profilerGroup != null) profilerGroup.attachToDispatcher(dispatcher);
        for (final var target : dispatcher.getCompatibleTargetTypes()) {
            final var compKey  = EventTargetKey.of(dispatcher.getEventType(), target);
            final var collection = findListeners(compKey);
            if (collection != null) dispatcher.setListenerCollection(collection);
        }
        return dispatcher;
    }

    @Override
    public <E extends Event, T extends ComponentTarget>
    @NotNull EventListenerCollection<E, T> getListenerCollection(
            @NotNull TypeRef<E> eventType,
            @NotNull TargetType<T> targetType) {

        final var key = EventTargetKey.of(eventType, targetType);
        final var found = findListeners(key);
        return found != null ? found : listeners.getCollection(key);
    }

    @Override
    protected <E extends Event, T extends ComponentTarget>
    @NotNull EventListenerCollection<E, T> buildListenerCollection(@NotNull EventTargetKey<E, T> key) {
        final var collection = super.buildListenerCollection(key);
        for (final var target : key.getTargetType().getSelfAndAllParents()) {
            for (final var link : targetGraph.get(target).getProvidedBy()) {
                bindCollectionsFromDestination(key, collection, link);
            }
        }
        return collection;
    }

    protected <E extends Event, T extends ComponentTarget>
    @Nullable EventListenerCollection<E, T> findListeners(@NotNull EventTargetKey<E, T> key) {
        final var existing = listeners.getCollectionOrNull(key);
        if (existing != null) return existing;

        EventListenerCollection<E, T> collection = null;
        for (final var target : key.getTargetType().getAllSubtargets()) {
            for (final var link : targetGraph.get(target).getProviding()) {
                //collection = bindCollectionsFromSource(key, collection, link); // TODO
            }
        }

        return collection;
    }

    private <E extends Event, A extends ComponentTarget, B extends ComponentTarget>
    EventListenerCollection<E, B> bindCollectionsFromSource(
            @NotNull EventTargetKey<E, B> key,
            @Nullable EventListenerCollection<E, B> collection,
            @NotNull LinkingComponentType<A, B> link) {

        final EventTargetKey<E, A> linkKey = EventTargetKey.of(key.getEventType(), link.getInnerTargetType());
        final var linkTo = listeners.getCollectionOrNull(linkKey);
        if (linkTo != null) {
            if (collection == null) collection = listeners.getCollection(key);
            ForwardingObserver.<E, A, B>bind(linkTo, collection, link);
        }
        return collection;
    }

    private <E extends Event, A extends ComponentTarget, B extends ComponentTarget>
    EventListenerCollection<E, A> bindCollectionsFromDestination(
            @NotNull EventTargetKey<E, A> key,
            @Nullable EventListenerCollection<E, A> collection,
            @NotNull LinkingComponentType<? super A, B> link) {

        final var linkKey = EventTargetKey.of(key.getEventType(), link.getTargetType());
        final var linkTo = listeners.getCollectionOrNull(linkKey);
        if (linkTo != null) {
            if (collection == null) collection = listeners.getCollection(key);
            ForwardingObserver.bind(collection, linkTo, link);
        }
        return collection;
    }

}
