package dev.m00nl1ght.clockwork.container;

import dev.m00nl1ght.clockwork.core.*;
import dev.m00nl1ght.clockwork.util.Arguments;

public class MutableComponentContainer<T extends ComponentTarget> extends ComponentContainer<T> {

    protected final Object[] components;
    protected final T object;

    public MutableComponentContainer(TargetType<T> targetType, T object) {
        super(targetType);
        this.components = new Object[targetType.getAllComponentTypes().size()];
        this.object = Arguments.notNull(object, "object");
        Arguments.verifyType(object.getClass(), targetType.getTargetClass(), "object");
    }

    public void initComponents() {
        for (var comp : targetType.getAllComponentTypes()) {
            try {
                final var idx = comp.getInternalIdx(targetType);
                if (components[idx] == null) {
                    components[idx] = buildComponent(comp, object);
                }
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
