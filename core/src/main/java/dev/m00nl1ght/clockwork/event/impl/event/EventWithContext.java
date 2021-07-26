package dev.m00nl1ght.clockwork.event.impl.event;

import dev.m00nl1ght.clockwork.component.ComponentContainer;
import dev.m00nl1ght.clockwork.core.ExceptionInPlugin;
import dev.m00nl1ght.clockwork.event.Event;
import dev.m00nl1ght.clockwork.event.impl.CompiledListeners;
import dev.m00nl1ght.clockwork.event.EventListener;
import dev.m00nl1ght.clockwork.util.FormatUtil;

public abstract class EventWithContext extends Event {

    private CompiledListeners currentListeners;
    private int currentListenerIdx = -1;

    @Override
    @SuppressWarnings("unchecked")
    public void post(ComponentContainer container, CompiledListeners listeners) {
        if (currentListeners != null) throw new IllegalStateException();
        currentListeners = listeners;
        final var consumers = listeners.consumers;
        final var cIdxs = listeners.cIdxs;
        final var length = consumers.length;
        for (currentListenerIdx = 0; currentListenerIdx < length; currentListenerIdx++) {
            final var component = container.getComponent(cIdxs[currentListenerIdx]);
            try {
                if (component != null) {
                    consumers[currentListenerIdx].accept(component, this);
                }
            } catch (ExceptionInPlugin e) {
                e.addComponentToStack(listeners.listeners[currentListenerIdx].getComponentType());
                throw e;
            } catch (Throwable e) {
                throw ExceptionInPlugin.inEventListener(listeners.listeners[currentListenerIdx], this, e);
            }
        }
    }

    protected EventListener<?, ?, ?> getCurrentListener() {
        try {
            return currentListeners.listeners[currentListenerIdx];
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            throw FormatUtil.rtExc("Event listener context is missing or currupted", e);
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
