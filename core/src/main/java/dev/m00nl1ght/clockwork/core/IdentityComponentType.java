package dev.m00nl1ght.clockwork.core;

public class IdentityComponentType<T extends ComponentTarget> extends ComponentType<T, T> {

    public IdentityComponentType(TargetType<T> targetType) {
        super(targetType.getParent() == null ? null : targetType.getParent().getIdentityComponentType(), targetType.getTargetClass(), targetType);
        setInternalIdx(0);
        setFactory(t -> t);
    }

    @Override
    public String toString() {
        return "<Identity>@" + targetType.toString();
    }

}
