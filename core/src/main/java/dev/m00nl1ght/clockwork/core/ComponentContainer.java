package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.debug.ProfilerEntry;
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
        for (var comp : targetType.getRegisteredTypes()) {
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
        for (var listener : listeners) {
            try {
                final var comp = components[listener.getComponentType().getInternalID()];
                if (comp != null) listener.accept(object, comp, event);
            } catch (ExceptionInPlugin e) {
                throw e;
            } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
                targetType.checkCompatibilityForEvent(eventType.getRootTarget());
                throw e;
            } catch (Throwable t) {
                throw ExceptionInPlugin.inEventHandler(listener.getComponentType(), event, object, t);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected <E> void post(EventType<E, T> eventType, E event, ProfilerEntry profilerEntry) {
        final var listeners = targetType.eventListeners[eventType.getInternalId()];
        for (var listener : listeners) {
            try {
                final var comp = components[listener.getComponentType().getInternalID()];
                if (comp != null) listener.accept(object, comp, event, profilerEntry);
            } catch (ExceptionInPlugin e) {
                throw e;
            } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
                targetType.checkCompatibilityForEvent(eventType.getRootTarget());
                throw e;
            } catch (Throwable t) {
                throw ExceptionInPlugin.inEventHandler(listener.getComponentType(), event, object, t);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected <F> void applySubtarget(FunctionalSubtarget<T, F> subtarget, Consumer<F> consumer) {
        final var compIds = targetType.subtargetData[subtarget.getInternalId()];
        for (var compId : compIds) {
            try {
                final var comp = components[compId];
                if (comp != null) consumer.accept((F) comp);
            } catch (ExceptionInPlugin e) {
                throw e;
            } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
                targetType.checkCompatibilityForSubtarget(subtarget.getRootTarget());
                throw e;
            } catch (Throwable t) {
                throw ExceptionInPlugin.inFunctionalSubtarget(targetType.components.get(compId), subtarget.getType(), t);
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
