package dev.m00nl1ght.clockwork.debug;

import dev.m00nl1ght.clockwork.core.*;
import dev.m00nl1ght.clockwork.debug.profiler.core.CoreProfiler;

import java.util.function.Consumer;

public class ProfilingComponentContainer<T extends ComponentTarget> extends ComponentContainer<T> {

    private final CoreProfiler profiler;

    public ProfilingComponentContainer(TargetType<T> targetType, T object, CoreProfiler profiler) {
        super(targetType, object);
        this.profiler = profiler;
    }

    @Override
    protected <E> void post(EventType<E, ? super T> eventType, E event) {
        super.post(eventType, event, profiler.getEntryFor(eventType, targetType));
    }

    @Override
    protected <F> void applySubtarget(FunctionalSubtarget<? super T, F> subtarget, Consumer<F> consumer) {
        super.applySubtarget(subtarget, consumer, profiler.getEntryFor(subtarget, targetType));
    }

}
