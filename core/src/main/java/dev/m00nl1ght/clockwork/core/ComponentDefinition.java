package dev.m00nl1ght.clockwork.core;

import com.vdurmont.semver4j.Semver;
import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.*;

public final class ComponentDefinition {

    private final PluginDefinition parent;
    private final String id;
    private final Semver version;
    private final String componentClass;
    private final String targetId;
    private final List<DependencyDefinition> dependencies;
    private final List<String> processors;

    private final boolean optional;

    protected ComponentDefinition(PluginDefinition parent, String id, Semver version, String componentClass, String targetId, Collection<DependencyDefinition> dependencies, boolean optional, List<String> processors) {
        this.parent = Preconditions.notNull(parent, "parent");
        this.id = parent.subId(Preconditions.notNull(id, "component id"));
        this.version = Preconditions.notNull(version, "version");
        this.componentClass = Preconditions.notNullOrBlank(componentClass, "componentClass");
        this.targetId = Preconditions.notNullOrBlank(targetId, "targetId");
        this.dependencies = List.copyOf(Preconditions.notNull(dependencies, "dependencies"));
        this.processors = List.copyOf(Preconditions.notNull(processors, "processors"));
        this.optional = optional;
        this.parent.addComponentDefinition(this);
    }

    public String getId() {
        return id;
    }

    public Semver getVersion() {
        return version;
    }

    public String getComponentClass() {
        return componentClass;
    }

    public String getTargetId() {
        return targetId;
    }

    public List<DependencyDefinition> getDependencies() {
        return dependencies;
    }

    public boolean isOptional() {
        return optional;
    }

    public PluginDefinition getParent() {
        return parent;
    }

    public List<String> getProcessors() {
        return processors;
    }

    @Override
    public String toString() {
        return id + ":" + version;
    }

    public static Builder builder(PluginDefinition plugin, String componentId) {
        return new Builder(plugin, componentId);
    }

    private static String pluginId(String id) {
        final var i = id.indexOf(':');
        return i < 0 ? id : id.substring(0, i);
    }

    public static class Builder {

        protected final PluginDefinition plugin;
        protected final String componentId;
        protected String componentClass;
        protected String targetId;
        protected final Map<String, DependencyDefinition> dependencies = new HashMap<>();
        protected final List<String> processors = new ArrayList<>(3);
        protected boolean optional = false;

        protected Builder(PluginDefinition plugin, String componentId) {
            this.plugin = plugin;
            this.componentId = componentId;
        }

        public ComponentDefinition build() {
            dependencies.computeIfAbsent(plugin.getId(), DependencyDefinition::buildAnyVersion);
            if (targetId != null) dependencies.computeIfAbsent(pluginId(targetId), DependencyDefinition::buildAnyVersion);
            return new ComponentDefinition(plugin, componentId, plugin.getVersion(), componentClass, targetId, dependencies.values(), optional, processors);
        }

        public Builder component(String componentClass) {
            this.componentClass = componentClass;
            return this;
        }

        public Builder target(String targetId) {
            this.targetId = targetId;
            return this;
        }

        public Builder dependency(DependencyDefinition dependency) {
            final var prev = this.dependencies.putIfAbsent(dependency.getComponentId(), dependency);
            if (prev != null) throw PluginLoadingException.generic("Duplicate dependency: [] Already present: []", dependency, prev);
            return this;
        }

        public Builder optional(boolean optional) {
            this.optional = optional;
            return this;
        }

        public Builder optional() {
            return optional(true);
        }

        public Builder markForProcessor(String processor) {
            this.processors.add(processor);
            return this;
        }

    }

}
