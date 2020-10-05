package dev.m00nl1ght.clockwork.core;

public class IdentityComponentType<T extends ComponentTarget> extends ComponentType<T, T> {

    public IdentityComponentType(IdentityComponentType<? super T> parent, TargetType<T> targetType) {
        super(parent, targetType.getTargetClass(), targetType);
        setFactory(t -> t);
    }

    @Override
    public String toString() {
        return "<Identity>@" + targetType.toString();
    }

}
