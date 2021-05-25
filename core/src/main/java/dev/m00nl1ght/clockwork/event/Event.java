package dev.m00nl1ght.clockwork.event;

import dev.m00nl1ght.clockwork.core.ComponentContainer;
import dev.m00nl1ght.clockwork.core.ExceptionInPlugin;
import dev.m00nl1ght.clockwork.event.impl.CompiledListeners;
import org.jetbrains.annotations.NotNull;

public abstract class Event {

    @SuppressWarnings("unchecked")
    public void post(@NotNull ComponentContainer container, @NotNull CompiledListeners listeners) {
        final var consumers = listeners.consumers;
        final var cIdxs = listeners.cIdxs;
        final var length = consumers.length;
        for (int i = 0; i < length; i++) {
            final var component = container.getComponent(cIdxs[i]);
            try {
                if (component != null) {
                    consumers[i].accept(component, this);
                }
            } catch (ExceptionInPlugin e) {
                e.addComponentToStack(listeners.listeners[i].getComponentType());
                throw e;
            } catch (Throwable e) {
                throw ExceptionInPlugin.inEventListener(listeners.listeners[i], this, e);
            }
        }
    }

}
