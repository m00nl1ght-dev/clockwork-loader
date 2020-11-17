package dev.m00nl1ght.clockwork.events.impl;

import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ExceptionInPlugin;
import dev.m00nl1ght.clockwork.events.CompiledListeners;
import dev.m00nl1ght.clockwork.events.Event;
import dev.m00nl1ght.clockwork.events.listener.EventListener;
import dev.m00nl1ght.clockwork.util.FormatUtil;

public abstract class ContextAwareEvent extends Event {

    private CompiledListeners currentListeners;
    private int currentListenerIdx = -1;

    @Override
    @SuppressWarnings("unchecked")
    public void post(ComponentTarget target, CompiledListeners listeners) {
        if (currentListeners != null) throw new IllegalStateException();
        currentListeners = listeners;
        final var consumers = listeners.consumers;
        final var cIdxs = listeners.cIdxs;
        final var length = consumers.length;
        for (currentListenerIdx = 0; currentListenerIdx < length; currentListenerIdx++) {
            final var component = target.getComponent(cIdxs[currentListenerIdx]);
            try {
                if (component != null) {
                    consumers[currentListenerIdx].accept(component, this);
                }
            } catch (ExceptionInPlugin e) {
                e.addComponentToStack(listeners.listeners[currentListenerIdx].getComponentType());
                throw e;
            } catch (Throwable e) {
                throw ExceptionInPlugin.inEventListener(listeners.listeners[currentListenerIdx], this, target, e);
            }
        }
    }

    protected EventListener<?, ?, ?> getCurrentListener() {
        try {
            return currentListeners.listeners[currentListenerIdx];
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            throw FormatUtil.rtExc("Event listener context is missing or currupted");
        }
    }

    protected void checkModificationAllowed() {
        if (currentListeners == null) return;
        final var priority = getCurrentListener().getPriority();
        if (!priority.isModificationAllowed()) {
            throw FormatUtil.illStateExc("Event can not be modified in state []", priority);
        }
    }

}
