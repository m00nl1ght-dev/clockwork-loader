package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Preconditions;
import dev.m00nl1ght.clockwork.util.ReflectionUtil;

public class ComponentType<C, T extends ComponentTarget> {

    private final LoadedPlugin plugin;
    private final ComponentDescriptor descriptor;
    private final Class<C> componentClass;
    private final TargetType<T> targetType;

    private int internalID = -1;
    private ComponentFactory<T, C> factory;

    ComponentType(LoadedPlugin plugin, ComponentDescriptor descriptor, Class<C> componentClass, TargetType<T> targetType) {
        this.descriptor = Preconditions.notNull(descriptor, "descriptor");
        this.plugin = Preconditions.notNullAnd(plugin, o -> o.getId().equals(descriptor.getPlugin().getId()), "plugin");
        this.targetType = Preconditions.notNullAnd(targetType, o -> o.getId().equals(descriptor.getTargetId()), "targetType");
        this.componentClass = Preconditions.notNullAnd(componentClass, o -> o.getName().equals(descriptor.getComponentClass()), "componentClass");
        this.factory = buildDefaultFactory(componentClass);
    }

    public LoadedPlugin getPlugin() {
        return plugin;
    }

    public ComponentDescriptor getDescriptor() {
        return descriptor;
    }

    public String getId() {
        return descriptor.getId();
    }

    public TargetType<T> getTargetType() {
        return targetType;
    }

    public Class<C> getComponentClass() {
        return componentClass;
    }

    @Override
    public String toString() {
        return descriptor.toString();
    }

    public int getInternalID() {
        return internalID;
    }

    @SuppressWarnings("unchecked")
    public C get(T object) {
        final var container = (ComponentContainer<T>) object.getComponentContainer();
        try {
            return (C) container.getComponent(internalID);
        } catch (ClassCastException | ArrayIndexOutOfBoundsException e) {
            container.getTargetType().checkCompatibility(this);
            throw e;
        }
    }

    protected void init(int internalID) { // TODO can this be avoided?
        if (this.internalID >= 0) throw new IllegalStateException();
        this.internalID = internalID;
    }

    public void setFactory(ComponentFactory<T, C> factory) {
        this.factory = factory;
    }

    protected C buildComponentFor(T object) throws Exception {
        return factory == null ? null : factory.create(object);
    }

    @SuppressWarnings("Convert2MethodRef")
    private ComponentFactory<T, C> buildDefaultFactory(Class<C> componentClass) {
        final var objCtr = ReflectionUtil.getConstructorOrNull(componentClass, targetType.getTargetClass());
        if (objCtr != null) return o -> objCtr.newInstance(o);
        final var emptyCtr = ReflectionUtil.getConstructorOrNull(componentClass);
        if (emptyCtr != null) return o -> emptyCtr.newInstance();
        return null;
    }

    public interface ComponentFactory<T, C> {
        C create(T obj) throws Exception;
    }

}
