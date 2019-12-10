package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.debug.profiler.core.EventProfilerGroup;
import dev.m00nl1ght.clockwork.debug.profiler.core.SubtargetProfilerGroup;
import dev.m00nl1ght.clockwork.event.listener.EventListener;
import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
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

    protected <E> void post(EventType<E, ? super T> eventType, E event) {
        final var listeners = targetType.eventListeners[eventType.getInternalId()];
        for (int i = 0; i < listeners.length; i++) {
            final var listener = listeners[i];
            try {
                final var comp = components[listener.getComponentType().getInternalID()];
                if (comp != null) listener.accept(object, comp, event);
            } catch (ExceptionInPlugin e) {
                throw e;
            } catch (Throwable t) {
                targetType.checkCompatibility(eventType);
                throw ExceptionInPlugin.inEventHandler(listener.getComponentType(), event, object, t);
            }
        }
    }

    protected <E> void post(EventType<E, ? super T> eventType, E event, EventProfilerGroup<E, T> profilerGroup) {
        final var listeners = targetType.eventListeners[eventType.getInternalId()];
        for (int i = 0; i < listeners.length; i++) {
            final var listener = listeners[i];
            try {
                final var comp = components[listener.getComponentType().getInternalID()];
                if (comp != null) listener.accept(object, comp, event, profilerGroup.get(i));
            } catch (ExceptionInPlugin e) {
                throw e;
            } catch (Throwable t) {
                targetType.checkCompatibility(eventType);
                throw ExceptionInPlugin.inEventHandler(listener.getComponentType(), event, object, t);
            }
        }
    }

    protected <E> List<EventListener<E, ?, T>> getListeners(EventType<E, ? super T> eventType) {
        return List.of(targetType.eventListeners[eventType.getInternalId()]);
    }

    protected <F> void applySubtarget(FunctionalSubtarget<? super T, F> subtarget, Consumer<F> consumer) {
        final var compIds = targetType.subtargetData[subtarget.getInternalId()];
        for (int i = 0; i < compIds.length; i++) {
            try {
                final var comp = components[compIds[i]];
                if (comp != null) consumer.accept((F) comp);
            } catch (ExceptionInPlugin e) {
                throw e;
            } catch (Throwable t) {
                targetType.checkCompatibility(subtarget);
                throw ExceptionInPlugin.inFunctionalSubtarget(targetType.components.get(compIds[i]), subtarget.getType(), t);
            }
        }
    }

    protected <F> void applySubtarget(FunctionalSubtarget<? super T, F> subtarget, Consumer<F> consumer, SubtargetProfilerGroup<T, F> profilerGroup) {
        final var compIds = targetType.subtargetData[subtarget.getInternalId()];
        for (int i = 0; i < compIds.length; i++) {
            try {
                final var comp = components[compIds[i]];
                if (comp != null) {
                    final var profilerEntry = profilerGroup.get(i);
                    final long t = System.nanoTime();
                    consumer.accept((F) comp);
                    profilerEntry.put(System.nanoTime() - t);
                }
            } catch (ExceptionInPlugin e) {
                throw e;
            } catch (Throwable t) {
                targetType.checkCompatibility(subtarget);
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
