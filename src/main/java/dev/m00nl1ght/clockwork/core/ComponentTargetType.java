package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Preconditions;

public final class ComponentTargetType<T extends ComponentTarget<T>> {

    private final String id;
    private final Class<T> targetClass;
    private final PluginContainer<?> parent;

    public ComponentTargetType(ComponentTargetDefinition definition, PluginContainer<?> parent, Class<T> targetClass) {
        this.parent = Preconditions.notNull(parent, "parent");
        Preconditions.notNull(definition, "definition");
        this.targetClass = Preconditions.notNullAnd(targetClass, o -> definition.getTargetClass().equals(o.getCanonicalName()), "targetClass");
        this.id = definition.getId();
    }

    public PluginContainer<?> getParent() {
        return parent;
    }

    public String getId() {
        return id;
    }

    public Class<T> getTargetClass() {
        return targetClass;
    }

}
