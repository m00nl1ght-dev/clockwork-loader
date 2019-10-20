package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Preconditions;

public final class ComponentTargetDefinition {

    private final PluginDefinition parent;
    private final String id;
    private final String targetClass;

    public ComponentTargetDefinition(PluginDefinition parent, String id, String targetClass) {
        this.parent = Preconditions.notNull(parent, "parent");
        this.id = parent.subId(Preconditions.notNull(id, "component target id"));
        this.targetClass = Preconditions.notNullOrBlank(targetClass, "targetClass");
        this.parent.addTargetDefinition(this);
    }

    public static ComponentTargetDefinition build(PluginDefinition parent, String id, String targetClass) {
        return new ComponentTargetDefinition(parent, id, targetClass);
    }

    public PluginDefinition getParent() {
        return parent;
    }

    public String getId() {
        return id;
    }

    public String getTargetClass() {
        return targetClass;
    }

}
