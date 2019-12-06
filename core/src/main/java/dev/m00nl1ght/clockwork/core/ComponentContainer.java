package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.debug.profiler.core.EventTypeProfilerGroup;
import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.function.Consumer;

public class ComponentContainer<T extends ComponentTarget> {

    protected final TargetType<T> targetType;
    protected final Object[] components;
    protected final T object;

    public ComponentContainer(TargetType<T> targetType, T object) {
        this.targetType = Preconditions.notNullAnd(targetType, TargetType::isInitialised, "targetType");
        Preconditions.verifyType(Preconditions.notNull(object, "object").getClass(), targetType.getTargetClass(), "object");
        this.components = new Object[targetType.getComponentCount()];
        this.object = object;
        this.initComponents();
    }

    protected void initComponents() {
        for (var comp : targetType.getComponentTypes()) {
            try {
                components[comp.getInternalID()] = comp.buildComponentFor(object);
            } catch (Throwable t) {
                throw ExceptionInPlugin.inComponentInit(comp, t);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected <E> void post(EventType<E, T> eventType, E event) {
        final var listeners = targetType.eventListeners[eventType.getInternalId()];
        for (int i = 0; i < listeners.length; i++) {
            final var listener = listeners[i];
            try {
                final var comp = components[listener.getComponentType().getInternalID()];
                if (comp != null) listener.accept(object, comp, event);
            } catch (ExceptionInPlugin e) {
                throw e;
            } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
                targetType.checkCompatibilityForEvent(eventType.getTargetType());
                throw e;
            } catch (Throwable t) {
                throw ExceptionInPlugin.inEventHandler(listener.getComponentType(), event, object, t);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected <E> void post(EventType<E, T> eventType, E event, EventTypeProfilerGroup<T> profilerGroup) {
        final var listeners = targetType.eventListeners[eventType.getInternalId()];
        for (int i = 0; i < listeners.length; i++) {
            final var listener = listeners[i];
            try {
                final var comp = components[listener.getComponentType().getInternalID()];
                if (comp != null) listener.accept(object, comp, event, profilerGroup.get(i));
            } catch (ExceptionInPlugin e) {
                throw e;
            } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
                targetType.checkCompatibilityForEvent(eventType.getTargetType());
                throw e;
            } catch (Throwable t) {
                throw ExceptionInPlugin.inEventHandler(listener.getComponentType(), event, object, t);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected <F> void applySubtarget(FunctionalSubtarget<T, F> subtarget, Consumer<F> consumer) {
        final var compIds = targetType.subtargetData[subtarget.getInternalId()];
        for (int i = 0; i < compIds.length; i++) {
            try {
                final var comp = components[compIds[i]];
                if (comp != null) consumer.accept((F) comp);
            } catch (ExceptionInPlugin e) {
                throw e;
            } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
                targetType.checkCompatibilityForSubtarget(subtarget.getTargetType());
                throw e;
            } catch (Throwable t) {
                throw ExceptionInPlugin.inFunctionalSubtarget(targetType.components.get(compIds[i]), subtarget.getType(), t);
            }
        }
    }

    protected Object getComponent(int internalID) {
        return components[internalID];
    }

    public TargetType<T> getTargetType() {
        return targetType;
    }

}
