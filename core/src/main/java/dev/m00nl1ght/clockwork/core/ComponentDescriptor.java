package dev.m00nl1ght.clockwork.core;

import dev.m00nl1ght.clockwork.util.Preconditions;
import dev.m00nl1ght.clockwork.version.Version;

import java.util.*;

public final class ComponentDescriptor {

    private final PluginDescriptor plugin;
    private final String id;
    private final String targetId;
    private final String componentClass;
    private final List<DependencyDescriptor> dependencies;
    private final boolean optional;

    ComponentDescriptor(Builder builder) {
        this.plugin = Preconditions.notNull(builder.plugin, "plugin");
        this.id = Preconditions.notNullOrBlank(builder.id, "id");
        this.targetId = Preconditions.notNullOrBlank(builder.targetId, "");
        this.componentClass = Preconditions.notNullOrBlank(builder.componentClass, "componentClass");
        this.dependencies = List.copyOf(Preconditions.notNull(builder.dependencies, "dependencies").values());
        this.optional = builder.optional;
    }

    public PluginDescriptor getPlugin() {
        return plugin;
    }

    public String getId() {
        return id;
    }

    public Version getVersion() {
        return plugin.getVersion();
    }

    public String getTargetId() {
        return targetId;
    }

    public String getComponentClass() {
        return componentClass;
    }

    public List<DependencyDescriptor> getDependencies() {
        return dependencies;
    }

    public boolean isOptional() {
        return optional;
    }

    @Override
    public String toString() {
        return getId() + ":" + getVersion();
    }

    public static Builder builder(PluginDescriptor plugin) {
        return new Builder(plugin);
    }

    public static final class Builder {

        private final PluginDescriptor plugin;
        private String id;
        private String componentClass;
        private String targetId;
        private final Map<String, DependencyDescriptor> dependencies = new LinkedHashMap<>();
        private boolean optional = false;

        private Builder(PluginDescriptor plugin) {
            this.plugin = plugin;
        }

        public ComponentDescriptor build() {
            this.addTrivialDeps();
            return new ComponentDescriptor(this);
        }

        private void addTrivialDeps() {
            if (!plugin.getId().equals(ClockworkCore.CORE_PLUGIN_ID)) {
                dependencies.computeIfAbsent(ClockworkCore.CORE_PLUGIN_ID, DependencyDescriptor::buildAnyVersion);
                if (!id.equals(plugin.getId())) dependencies.computeIfAbsent(plugin.getId(), DependencyDescriptor::buildAnyVersion);
                if (targetId != null) dependencies.computeIfAbsent(pluginId(targetId), DependencyDescriptor::buildAnyVersion);
            }
        }

        public Builder id(String id) {
            if (id == null) return this;
            if (id.equals(plugin.getId())) {this.id = id; return this;}
            if (!id.contains(":")) id = plugin.getId() + ":" + id;
            final var matcher = DependencyDescriptor.COMPONENT_ID_PATTERN.matcher(id);
            if (matcher.matches()) {
                if (matcher.group(1).equals(plugin.getId())) {
                    this.id = id;
                    return this;
                } else {
                    throw PluginLoadingException.subIdMismatch(plugin, id);
                }
            } else {
                throw PluginLoadingException.invalidId(id);
            }
        }

        public Builder componentClass(String componentClass) {
            this.componentClass = componentClass;
            return this;
        }

        public Builder target(String targetId) {
            this.targetId = targetId;
            return this;
        }

        public Builder dependency(DependencyDescriptor dependency) {
            final var prev = this.dependencies.putIfAbsent(dependency.getTarget(), dependency);
            if (prev != null) throw PluginLoadingException.dependencyDuplicate(id, dependency, prev);
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

    private static String pluginId(String id) {
        final var i = id.indexOf(':');
        return i < 0 ? id : id.substring(0, i);
    }

}
