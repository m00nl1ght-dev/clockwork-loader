package dev.m00nl1ght.clockwork.benchmarks.event;

import dev.m00nl1ght.clockwork.benchmarks.TestEvent;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.events.BasicEventType;
import dev.m00nl1ght.clockwork.events.EventListener;
import dev.m00nl1ght.clockwork.util.TypeRef;

import java.util.Arrays;
import java.util.function.BiConsumer;

public class EventTypeImpl3<E extends TestEvent, T extends ComponentTarget> extends BasicEventType<E, T> {

    private static final int[] EMPTY_IDX_ARRAY = new int[0];
    private static final BiConsumer[] EMPTY_CONSUMER_ARRAY = new BiConsumer[0];

    private BiConsumer[][] consumers;
    private int[][] cIdxs;

    public EventTypeImpl3(TypeRef<E> eventClassType, Class<T> targetClass) {
        super(eventClassType, targetClass);
    }

    public EventTypeImpl3(Class<E> eventClass, Class<T> targetClass) {
        super(eventClass, targetClass);
    }

    public EventTypeImpl3(TypeRef<E> eventClassType, TargetType<T> targetType) {
        super(eventClassType, targetType);
    }

    public EventTypeImpl3(Class<E> eventClass, TargetType<T> targetType) {
        super(eventClass, targetType);
    }

    @Override
    protected void init() {
        super.init();
        final var cnt = getTargetType().getSubtargetIdxLast() - idxOffset + 1;
        this.consumers = new BiConsumer[cnt][];
        Arrays.fill(consumers, EMPTY_CONSUMER_ARRAY);
        this.cIdxs = new int[cnt][];
        Arrays.fill(cIdxs, EMPTY_IDX_ARRAY);
    }

    @Override
    protected void onListenersChanged(TargetType<? extends T> targetType) {
        final var listeners = getEffectiveListeners(targetType);
        final var idx = targetType.getSubtargetIdxFirst() - idxOffset;
        this.consumers[idx] = listeners.stream().map(EventListener::getConsumer).toArray(BiConsumer[]::new);
        this.cIdxs[idx] = listeners.stream().mapToInt(EventListener::getComponentIdx).toArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public E post(T object, E event) {
        final var container = object.getComponentContainer();
        final var target = container.getTargetType();
        if (target.getRoot() != rootTarget) checkCompatibility(target);
        try {
            final var idx = target.getSubtargetIdxFirst() - idxOffset;
            event.listeners = listeners[idx];
            final var cons = consumers[idx];
            final var idxs = cIdxs[idx];
            for (int i = 0; i < cons.length; i++) {
                event.lIdx = i;
                final var component = container.getComponent(idxs[i]);
                if (component != null) cons[i].accept(component, event);
            }
            return event;
        } catch (Throwable t) {
            checkCompatibility(target);
            throw t;
        }
    }

}
