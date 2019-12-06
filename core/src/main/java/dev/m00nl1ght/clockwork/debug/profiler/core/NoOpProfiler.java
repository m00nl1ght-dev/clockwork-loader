package dev.m00nl1ght.clockwork.debug.profiler.core;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.EventType;

public class NoOpProfiler extends CoreProfiler {

    public NoOpProfiler(ClockworkCore core) {
        super(core);
    }

    @Override
    protected void init() {}

    @Override
    public <E, T extends ComponentTarget> E postEvent(EventType<E, T> eventType, T object, E event) {
        return eventType.post(object, event);
    }

}
