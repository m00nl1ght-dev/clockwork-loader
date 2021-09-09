package dev.m00nl1ght.clockwork.event;

import dev.m00nl1ght.clockwork.component.ComponentTarget;
import dev.m00nl1ght.clockwork.component.ComponentType;
import dev.m00nl1ght.clockwork.utils.reflect.TypeRef;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.BiConsumer;

public abstract class EventListener<E extends Event, T extends ComponentTarget, C> {

    public static final Comparator<EventListener<?, ?, ?>> PHASE_ORDER =
            Comparator.comparingInt(o -> o.phase.ordinal());

    protected final ComponentType<C, T> componentType;
    protected final TypeRef<E> eventType;
    protected final Phase phase;

    protected EventListener(@NotNull TypeRef<E> eventType,
                            @NotNull ComponentType<C, T> componentType,
                            @NotNull EventListener.Phase phase) {

        this.componentType = Objects.requireNonNull(componentType);
        this.phase = Objects.requireNonNull(phase);
        this.eventType = Objects.requireNonNull(eventType);
    }

    public @NotNull TypeRef<E> getEventType() {
        return eventType;
    }

    public @NotNull ComponentType<C, T> getComponentType() {
        return componentType;
    }

    public @NotNull EventListener.Phase getPriority() {
        return phase;
    }

    public abstract @NotNull BiConsumer<C, E> getConsumer();

    public abstract @NotNull String getUniqueID();

    @Override
    public String toString() {
        return eventType + " [" + phase + "] -> " + componentType + " (" + getUniqueID() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventListener)) return false;
        EventListener<?, ?, ?> that = (EventListener<?, ?, ?>) o;
        return componentType.equals(that.componentType) &&
                eventType.equals(that.eventType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentType, eventType);
    }

    public enum Phase {

        PRE(false),
        EARLY(true),
        NORMAL(true),
        LATE(true),
        POST(false);

        private final boolean modificationAllowed;

        Phase(boolean modificationAllowed) {
            this.modificationAllowed = modificationAllowed;
        }

        public boolean isModificationAllowed() {
            return modificationAllowed;
        }

    }

}
