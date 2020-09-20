package dev.m00nl1ght.clockwork.container;

import dev.m00nl1ght.clockwork.core.ComponentContainer;
import dev.m00nl1ght.clockwork.core.ComponentTarget;
import dev.m00nl1ght.clockwork.core.ExceptionInPlugin;
import dev.m00nl1ght.clockwork.core.TargetType;
import dev.m00nl1ght.clockwork.util.Arguments;

public class ImmutableComponentContainer<T extends ComponentTarget> extends ComponentContainer<T> {

    protected final Object[] components;

    public ImmutableComponentContainer(TargetType<T> targetType, T object) {
        super(targetType);
        this.components = new Object[targetType.getComponentTypes().size()];
        this.components[0] = Arguments.notNull(object, "object");
        Arguments.verifyType(object.getClass(), targetType.getTargetClass(), "object");
    }

    public void initComponents() {
        for (var comp : targetType.getComponentTypes()) {
            try {
                final var idx = comp.getInternalIdx(targetType);
                if (components[idx] == null) {
                    components[idx] = buildComponent(comp);
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
