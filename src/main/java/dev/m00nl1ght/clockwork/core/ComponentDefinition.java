package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ComponentDefinition {

    private final PluginDefinition parent;
    private final String id;
    private final String version;
    private final String componentClass;
    private final String targetId;
    private final List<DependencyDefinition> dependencies;

    private final boolean optional;

    public ComponentDefinition(PluginDefinition parent, String id, String version, String componentClass, String targetId, List<DependencyDefinition> dependencies, boolean optional) {
        this.parent = Preconditions.notNull(parent, "parent");
        this.id = parent.subId(Preconditions.notNull(id, "component id"));
        this.version = Preconditions.notNullOrBlank(version, "version"); //TODO verify semver
        this.componentClass = Preconditions.notNullOrBlank(componentClass, "componentClass");
        this.targetId = Preconditions.notNullOrBlank(targetId, "targetId");
        this.dependencies = Collections.unmodifiableList(Preconditions.notNull(dependencies, "dependencies"));
        this.optional = optional;
        this.parent.addComponent(this);
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
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

    @Override
    public String toString() {
        return id + ":" + version + " [" + targetId + "]";
    }

    public static Builder builder(PluginDefinition plugin, String componentId) {
        return new Builder(plugin, componentId);
    }

    public static class Builder {

        protected final PluginDefinition plugin;
        protected final String componentId;
        protected String componentClass;
        protected String targetId;
        protected final List<DependencyDefinition> dependencies = new ArrayList<>();
        protected boolean optional = false;

        protected Builder(PluginDefinition plugin, String componentId) {
            this.plugin = plugin;
            this.componentId = componentId;
        }

        public ComponentDefinition build() {
            return new ComponentDefinition(plugin, componentId, plugin.getVersion(), componentClass, targetId, dependencies, optional);
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
            this.dependencies.add(dependency);
            return this;
        }

        public Builder optional(boolean optional) {
            this.optional = optional;
            return this;
        }

        public Builder optional() {
            return optional(true);
        }

    }

}
