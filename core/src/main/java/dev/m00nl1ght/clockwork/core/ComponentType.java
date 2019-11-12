package dev.m00nl1ght.clockwork.core;

import com.vdurmont.semver4j.Semver;
import dev.m00nl1ght.clockwork.util.Preconditions;
import dev.m00nl1ght.clockwork.util.ReflectionUtil;

public final class ComponentType<C, T> {

    private final String componentId;
    private final Semver version;
    private final Class<C> componentClass;
    private final PluginContainer plugin;
    private final ComponentTargetType<T> targetType;
    private final int internalID;
    private ComponentFactory<T, C> factory;

    protected ComponentType(ComponentDefinition definition, PluginContainer plugin, Class<C> componentClass, ComponentTargetType<T> targetType, int internalID) {
        Preconditions.notNull(definition, "definition");
        this.componentId = definition.getId();
        this.internalID = internalID;
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

    public ComponentTargetType<T> getTargetType() {
        return targetType;
    }

    public Class<C> getComponentClass() {
        return componentClass;
    }

    protected int getInternalID() {
        return internalID;
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
