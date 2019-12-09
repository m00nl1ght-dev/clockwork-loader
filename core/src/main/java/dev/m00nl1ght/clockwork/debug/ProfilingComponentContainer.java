package dev.m00nl1ght.clockwork.debug;

import dev.m00nl1ght.clockwork.core.ComponentContainer;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.debug.profiler.core.CoreProfiler;

public class ProfilingComponentContainer<T extends ComponentTarget> extends ComponentContainer<T> {

    private final CoreProfiler profiler;

    public ProfilingComponentContainer(TargetType<T> targetType, T object, CoreProfiler profiler) {
        super(targetType, object);
        this.profiler = profiler;
    }


    //TODO

}
