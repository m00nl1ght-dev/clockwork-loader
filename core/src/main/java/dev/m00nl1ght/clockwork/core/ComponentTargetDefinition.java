package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.List;

public final class ComponentTargetDefinition {

    private final PluginDefinition plugin;
    private final String parent;
    private final String id;
    private final String targetClass;
    private final List<String> processors;

    public ComponentTargetDefinition(PluginDefinition plugin, String id, String parent, String targetClass, List<String> processors) {
        this.parent = parent;
        this.plugin = Preconditions.notNull(plugin, "parent");
        this.id = plugin.subId(Preconditions.notNull(id, "component target id"));
        this.targetClass = Preconditions.notNullOrBlank(targetClass, "targetClass");
        this.processors = List.copyOf(Preconditions.notNull(processors, "processors"));
        this.plugin.addTargetDefinition(this);
    }

    public static ComponentTargetDefinition build(PluginDefinition plugin, String id, String parent, String targetClass, String... processors) {
        return new ComponentTargetDefinition(plugin, id, parent, targetClass, List.of(processors));
    }

    public PluginDefinition getPlugin() {
        return plugin;
    }

    public String getId() {
        return id;
    }

    public String getParent() {
        return parent;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public List<String> getProcessors() {
        return processors;
    }

    @Override
    public String toString() {
        return id;
    }

}
