package dev.m00nl1ght.clockwork.core;

public class IdentityComponentType<T extends ComponentTarget> extends ComponentType<T, T> {

    public IdentityComponentType(TargetType<T> targetType) {
        super(targetType, targetType.getTargetClass());
        setFactory(t -> t);
    }

    @Override
    public void checkValue(T target, T value) {
        if (target != value) throw new RuntimeException("Invalid value: " + value);
    }

    @Override
    public String toString() {
        return "<Identity>@" + targetType.toString();
    }

}
