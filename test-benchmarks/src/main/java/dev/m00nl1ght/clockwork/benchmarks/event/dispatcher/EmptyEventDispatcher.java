package dev.m00nl1ght.clockwork.benchmarks.event.dispatcher;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ComponentType;
import dev.m00nl1ght.clockwork.events.Event;

import java.util.List;
import java.util.function.BiConsumer;

public class EmptyEventDispatcher implements EventDispatcher {

    public static final EventDispatcher INSTANCE = new EmptyEventDispatcher();

    private EmptyEventDispatcher() {}

    @Override
    public EventDispatcher addListener(ComponentType componentType, BiConsumer listener) {
        return null;
    }

    @Override
    public EventDispatcher removeListener(ComponentType componentType) {
        return null;
    }

    @Override
    public List<ComponentType> getListeners() {
        return List.of();
    }

    @Override
    public void post(ComponentTarget target, Event event) {
        // NO-OP
    }

}
