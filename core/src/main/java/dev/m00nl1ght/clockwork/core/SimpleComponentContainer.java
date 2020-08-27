package dev.m00nl1ght.clockwork.core;

public class SimpleComponentContainer<T extends ComponentTarget> extends ComponentContainer<T> {

    protected final Object[] components;

    public SimpleComponentContainer(TargetType<T> targetType, T object) {
        super(targetType, object);
        this.components = new Object[targetType.getAllComponentTypes().size()];
    }

    @Override
    public void initComponents() { // TODO restrict access (controller?)
        for (var comp : targetType.getAllComponentTypes()) {
            try {
                components[comp.getInternalID()] = buildComponent(comp);
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
