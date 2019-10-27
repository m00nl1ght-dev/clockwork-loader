package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.List;

public final class ComponentTargetDefinition {

    private final PluginDefinition parent;
    private final String id;
    private final String targetClass;
    private final List<String> processors;

    public ComponentTargetDefinition(PluginDefinition parent, String id, String targetClass, List<String> processors) {
        this.parent = Preconditions.notNull(parent, "parent");
        this.id = parent.subId(Preconditions.notNull(id, "component target id"));
        this.targetClass = Preconditions.notNullOrBlank(targetClass, "targetClass");
        this.processors = List.copyOf(Preconditions.notNull(processors, "processors"));
        this.parent.addTargetDefinition(this);
    }

    public static ComponentTargetDefinition build(PluginDefinition parent, String id, String targetClass, String... processors) {
        return new ComponentTargetDefinition(parent, id, targetClass, List.of(processors));
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

    public List<String> getProcessors() {
        return processors;
    }

}
