package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Preconditions;

public class ComponentType<C, T> {

    private final String componentId;
    private final String version;
    private final Class<C> componentClass;
    private final PluginContainer<?> parent;
    private final ComponentTargetType<T> targetType;

    protected ComponentType(ComponentDefinition definition, PluginContainer<?> parent, Class<C> componentClass, ComponentTargetType<T> targetType) {
        Preconditions.notNull(definition, "definition");
        this.componentId = definition.getId();
        this.version = definition.getVersion();
        this.parent = Preconditions.notNullAnd(parent, o -> o.getId().equals(definition.getParent().getId()), "parent");
        this.targetType = Preconditions.notNullAnd(targetType, o -> definition.getTargetId().equals(o.getId()), "targetType");
        this.componentClass = Preconditions.notNullAnd(componentClass, o -> definition.getComponentClass().equals(o.getCanonicalName()), "componentClass");
    }

    public String getId() {
        return componentId;
    }

    public String getVersion() {
        return version;
    }

    public ComponentTargetType<T> getTargetType() {
        return targetType;
    }

    public Class<C> getComponentClass() {
        return componentClass;
    }

}
