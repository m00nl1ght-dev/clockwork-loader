package dev.m00nl1ght.clockwork.container;

import dev.m00nl1ght.clockwork.core.*;

import java.util.Objects;

public class MutableComponentContainer extends ComponentContainer {

    protected final Object[] components;

    public MutableComponentContainer(TargetType<?> targetType, Object object) {
        super(targetType);
        this.components = new Object[targetType.getComponentTypes().size()];
        this.components[0] = Objects.requireNonNull(object);
        if (!targetType.getTargetClass().isInstance(object))
            throw new IllegalArgumentException();
    }

    public void initComponents() {
        final var object = this.components[0];
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

    public <C, T extends ComponentTarget>
    void setComponent(ComponentType<C, T> componentType, C component) {
        final var castedTarget = componentType.getTargetType().getTargetClass().cast(this.components[0]);
        final var internalIdx = componentType.getInternalIdx(targetType);
        componentType.checkValue(castedTarget, component);
        components[internalIdx] = component;
    }

}
