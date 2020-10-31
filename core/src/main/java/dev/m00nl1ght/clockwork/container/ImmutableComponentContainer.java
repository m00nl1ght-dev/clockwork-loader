package dev.m00nl1ght.clockwork.container;

import dev.m00nl1ght.clockwork.core.ComponentContainer;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ExceptionInPlugin;
import dev.m00nl1ght.clockwork.core.TargetType;

import java.util.Objects;

public class ImmutableComponentContainer<T extends ComponentTarget> extends ComponentContainer<T> {

    protected final Object[] components;

    public ImmutableComponentContainer(TargetType<T> targetType, Object object) {
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

}
