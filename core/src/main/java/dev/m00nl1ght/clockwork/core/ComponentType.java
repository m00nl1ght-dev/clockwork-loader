package dev.m00nl1ght.clockwork.core;

import com.vdurmont.semver4j.Semver;
import dev.m00nl1ght.clockwork.util.Preconditions;
import dev.m00nl1ght.clockwork.util.ReflectionUtil;

public class ComponentType<C, T extends ComponentTarget> {

    private final String componentId;
    private final Semver version;
    private final Class<C> componentClass;
    private final PluginContainer plugin;
    private final TargetType<T> targetType;
    private int internalID = -1;
    private ComponentFactory<T, C> factory;

    ComponentType(ComponentDefinition definition, PluginContainer plugin, Class<C> componentClass, TargetType<T> targetType) {
        Preconditions.notNull(definition, "definition");
        this.componentId = definition.getId();
        this.version = definition.getVersion();
        this.plugin = Preconditions.notNullAnd(plugin, o -> o.getId().equals(definition.getParent().getId()), "parent");
        this.targetType = Preconditions.notNullAnd(targetType, o -> definition.getTargetId().equals(o.getId()), "targetType");
        this.componentClass = Preconditions.notNullAnd(componentClass, o -> definition.getComponentClass().equals(o.getCanonicalName()), "componentClass");
        this.factory = buildDefaultFactory(componentClass);
    }

    public String getId() {
        return componentId;
    }

    public Semver getVersion() {
        return version;
    }

    public PluginContainer getPlugin() {
        return plugin;
    }

    public TargetType<T> getTargetType() {
        return targetType;
    }

    public Class<C> getComponentClass() {
        return componentClass;
    }

    @Override
    public String toString() {
        return componentId;
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

    protected void init(int internalID) {
        if (this.internalID >= 0) throw new IllegalStateException();
        this.internalID = internalID;
    }

    public void setFactory(ComponentFactory<T, C> factory) {
        this.factory = factory;
    }

    public C buildComponentFor(T object) {
        try {
            return factory == null ? null : factory.create(object);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred trying to initialize component [" + getId() + "]", e);
        }
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
