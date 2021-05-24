package dev.m00nl1ght.clockwork.descriptor;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import dev.m00nl1ght.clockwork.core.PluginLoadingException;
import dev.m00nl1ght.clockwork.config.Config;
import dev.m00nl1ght.clockwork.version.Version;

import java.util.*;

public final class ComponentDescriptor {

    private final String pluginId;
    private final String componentId;
    private final Version version;
    private final String targetId;
    private final String componentClass;
    private final List<DependencyDescriptor> dependencies;
    private final boolean factoryAccessEnabled;
    private final boolean optional;
    private final Config extData;

    ComponentDescriptor(Builder builder) {
        this.pluginId = builder.pluginId;
        this.componentId = builder.componentId;
        this.version = Objects.requireNonNull(builder.version);
        this.targetId = Objects.requireNonNull(builder.targetId);
        this.componentClass = Objects.requireNonNull(builder.componentClass);
        this.dependencies = List.copyOf(builder.dependencies.values());
        this.factoryAccessEnabled = builder.factoryAccessEnabled;
        this.optional = builder.optional;
        this.extData = Objects.requireNonNull(builder.extData);
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getId() {
        return Namespaces.combine(pluginId, componentId);
    }

    public Version getVersion() {
        return version;
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

    public boolean isFactoryAccessEnabled() {
        return factoryAccessEnabled;
    }

    public Config getExtData() {
        return extData;
    }

    @Override
    public String toString() {
        return getId();
    }

    public static Builder builder(String pluginId) {
        return new Builder(Namespaces.simpleId(Objects.requireNonNull(pluginId)), null);
    }

    public static Builder builder(String pluginId, String componentId) {
        final var first = Namespaces.simpleId(Objects.requireNonNull(pluginId));
        final var second = Namespaces.second(Namespaces.combinedIdWithFirst(componentId, pluginId));
        return new Builder(first, second);
    }

    public static final class Builder {

        private final String pluginId;
        private final String componentId;
        private Version version;
        private String componentClass;
        private String targetId;
        private final Map<String, DependencyDescriptor> dependencies = new LinkedHashMap<>();
        private boolean factoryAccessEnabled = false;
        private boolean optional = false;
        private Config extData = Config.EMPTY;

        private Builder(String pluginId, String componentId) {
            this.pluginId = pluginId;
            this.componentId = componentId;
        }

        public ComponentDescriptor build() {
            this.addTrivialDeps();
            return new ComponentDescriptor(this);
        }

        public Builder version(Version version) {
            this.version = version;
            return this;
        }

        private void addTrivialDeps() {
            if (!pluginId.equals(ClockworkCore.CORE_PLUGIN_ID)) {
                dependencies.computeIfAbsent(ClockworkCore.CORE_PLUGIN_ID, DependencyDescriptor::buildAnyVersion);
                if (componentId != null) dependencies.computeIfAbsent(pluginId, DependencyDescriptor::buildAnyVersion);
                if (targetId != null) dependencies.computeIfAbsent(Namespaces.first(targetId), DependencyDescriptor::buildAnyVersion);
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
            if (prev != null) throw PluginLoadingException.dependencyDuplicate(Namespaces.combine(pluginId, componentId), dependency, prev);
            return this;
        }

        public Builder optional(boolean optional) {
            this.optional = optional;
            return this;
        }

        public Builder optional() {
            return optional(true);
        }

        public void factoryAccessEnabled(boolean factoryAccessEnabled) {
            this.factoryAccessEnabled = factoryAccessEnabled;
        }

        public void extData(Config extData) {
            this.extData = Objects.requireNonNull(extData);
        }

    }

}
