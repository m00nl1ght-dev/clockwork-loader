package dev.m00nl1ght.clockwork.component.impl;

import dev.m00nl1ght.clockwork.component.ComponentContainer;
import dev.m00nl1ght.clockwork.component.ComponentTarget;
import dev.m00nl1ght.clockwork.component.ComponentType;
import dev.m00nl1ght.clockwork.component.TargetType;
import dev.m00nl1ght.clockwork.core.ClockworkException;

import java.util.Objects;

public class SimpleComponentContainer extends ComponentContainer {

    protected final Object[] components;

    public SimpleComponentContainer(TargetType<?> targetType, Object object) {
        super(targetType);
        this.components = new Object[targetType.getComponentTypes().size()];
        this.components[0] = Objects.requireNonNull(object);
        if (!targetType.getTargetClass().isInstance(object))
            throw new IllegalArgumentException();
    }

    public void initComponents() {
        final var target = this.components[0];
        for (var comp : targetType.getComponentTypes()) {
            try {
                final var idx = comp.getInternalIdx();
                if (components[idx] == null) {
                    components[idx] = buildComponent(comp, target);
                }
            } catch (ClockworkException e) {
                e.addComponentToStack(comp);
                throw e;
            } catch (Throwable t) {
                throw ClockworkException.inComponentInit(comp, t);
            }
        }
    }

    protected <C, T extends ComponentTarget> C
    buildComponent(ComponentType<C, T> componentType, Object target) throws Throwable {
        final var castedTarget = componentType.getTargetType().getTargetClass().cast(target);
        final var value = componentType.getFactory().create(castedTarget);
        componentType.checkValue(castedTarget, value);
        return value;
    }

    @Override
    public Object getComponent(int internalID) {
        return components[internalID];
    }

}
