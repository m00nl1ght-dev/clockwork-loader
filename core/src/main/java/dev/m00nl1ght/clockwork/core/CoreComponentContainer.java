package dev.m00nl1ght.clockwork.core;

public class CoreComponentContainer extends ComponentContainer<ClockworkCore> {

    protected final Object[] components;

    CoreComponentContainer(TargetType<ClockworkCore> targetType, ClockworkCore core) {
        super(targetType, core);
        this.components = new Object[targetType.getAllComponentTypes().size()];
    }

    @Override
    public void initComponents() {
        object.getState().require(ClockworkCore.State.POPULATED);
        for (var comp : targetType.getOwnComponentTypes()) {
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
