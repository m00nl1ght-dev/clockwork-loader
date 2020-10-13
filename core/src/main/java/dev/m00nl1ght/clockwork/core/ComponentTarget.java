package dev.m00nl1ght.clockwork.core;

public interface ComponentTarget {

    TargetType<?> getTargetType();

    Object getComponent(int internalID);

    @SuppressWarnings("unchecked")
    static <T extends ComponentTarget> TargetType<T> typeOf(T target) {
        return (TargetType<T>) target.getTargetType();
    }

}
