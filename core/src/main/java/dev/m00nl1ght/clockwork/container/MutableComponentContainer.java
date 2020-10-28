package dev.m00nl1ght.clockwork.container;

import dev.m00nl1ght.clockwork.core.*;

import java.util.Objects;

public class MutableComponentContainer<T extends ComponentTarget> extends ComponentContainer<T> {

    protected final Object[] components;

    public MutableComponentContainer(TargetType<T> targetType, T object) {
        super(targetType);
        this.components = new Object[targetType.getComponentTypes().size()];
        this.components[0] = Objects.requireNonNull(object);
        if (!targetType.getTargetClass().isAssignableFrom(object.getClass()))
            throw new IllegalArgumentException();
    }

    public void initComponents() {
        final var object = getTarget();
        for (var comp : targetType.getComponentTypes()) {
            try {
                final var idx = comp.getInternalIdx();
                if (components[idx] == null) {
                    components[idx] = buildComponent(comp, object);
                }
            } catch (ExceptionInPlugin e) {
                e.addComponentToStack(comp);
                throw e;
            } catch (Throwable t) {
                throw ExceptionInPlugin.inComponentInit(comp, t);
            }
        }
    }

    @Override
    public Object getComponent(int internalID) {
        return components[internalID];
    }

    public <C> void setComponent(ComponentType<C, ? super T> componentType, C component) {
        components[componentType.getInternalIdx(targetType)] = component;
    }

}
